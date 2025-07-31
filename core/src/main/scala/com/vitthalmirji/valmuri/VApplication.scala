package com.vitthalmirji.valmuri

import com.vitthalmirji.valmuri.config.{VAutoConfig, VConfig}
import com.vitthalmirji.valmuri.error.FrameworkError
import com.vitthalmirji.valmuri.error.FrameworkError.{ConfigError, ServiceError}

import scala.reflect.ClassTag
import scala.util.Try

/**
 * Core framework trait - Fixed for cross-compilation
 */
trait VApplication {

  // Type aliases for better readability
  type ServiceContainer = VServices
  type AppConfiguration = VConfig
  type RouteHandler = VRequest => VResult[String]
  type ControllerList = List[VController]

  // Service registry - framework manages this (lazy initialization)
  private[valmuri] lazy val services: ServiceContainer = new VServices()

  // Configuration - loaded lazily like Spring Boot
  private[valmuri] lazy val config: AppConfiguration = loadConfiguration()

  // Auto-configuration - happens lazily and safely
  private[valmuri] lazy val autoConfig: VAutoConfig = new VAutoConfig(config, services)

  // Override these for custom configuration
  def configurationArgs(): Array[String] = Array.empty
  def configurationProfile(): String = ""

  // Register additional services (optional) - returns VResult for error handling
  def configure(): VResult[Unit] = VResult.success(())

  // Simple routing - can be routes OR controllers (covariant return type)
  def routes(): List[VRoute] = List.empty

  // Auto-wire controllers (optional) - with error handling
  def controllers(): VResult[ControllerList] = VResult.success(List.empty)

  /**
   * SPRING BOOT STYLE STARTUP - Everything auto-configured with error handling!
   */
  final def start(): VResult[Unit] = {
    for {
      _ <- VResult.fromTry(Try {
        println(s"🚀 Starting ${config.appName} v${config.appVersion}")
      })
      _ <- autoConfigureComponents()
      _ <- configure()
      allRoutes <- buildRoutes()
      finalRoutes <- addActuatorRoutes(allRoutes)
      server <- startServer(finalRoutes)
      _ <- VResult.fromTry(Try {
        println(s"✅ ${config.appName} running at http://${config.serverHost}:${config.serverPort}")
        if (config.actuatorEnabled) {
          println(s"📊 Actuator: http://${config.serverHost}:${config.serverPort}/actuator/health")
        }
        Thread.currentThread().join()
      })
    } yield ()
  }

  // Pattern matching for safe auto-configuration
  private def autoConfigureComponents(): VResult[Unit] = {
    VResult.fromTry(Try(autoConfig.configure())).recoverWith {
      case _: ConfigError => VResult.failure(FrameworkError.ConfigError("Configuration failed"))
      case _: ServiceError => VResult.failure(FrameworkError.ServiceError("Service error"))
      case ex => VResult.failure(FrameworkError.UnexpectedError(ex.message))
    }
  }

  private def loadConfiguration(): AppConfiguration = {
    VConfig.load(configurationArgs(), configurationProfile())
  }

  // Enhanced route building with error handling and pattern matching
  private def buildRoutes(): VResult[List[VRoute]] = {
    for {
      directRoutes <- VResult.success(routes())
      controllerList <- controllers()
      controllerRoutes <- extractControllerRoutes(controllerList)
    } yield directRoutes ++ controllerRoutes
  }

  // Pattern matching for controller route extraction
  private def extractControllerRoutes(controllers: ControllerList): VResult[List[VRoute]] = {
    VResult.fromTry(Try {
      controllers.flatMap { controller =>
        if (controller != null) controller.routes() else List.empty[VRoute]
      }
    })
  }

  // Conditional actuator route addition using pattern matching
  private def addActuatorRoutes(routes: List[VRoute]): VResult[List[VRoute]] = {
    if (config.actuatorEnabled) {
      buildActuatorRoutes().map(actuatorRoutes => routes ++ actuatorRoutes)
    } else {
      VResult.success(routes)
    }
  }

  private def buildActuatorRoutes(): VResult[List[VRoute]] = {
    VResult.fromTry(Try {
      val actuator = services.get[VActuator]
      List(
        VRoute("/actuator/health", _ => VResult.success(actuator.healthEndpoint())),
        VRoute("/actuator/metrics", _ => VResult.success(actuator.metricsEndpoint())),
        VRoute("/actuator/info", _ => VResult.success(actuator.infoEndpoint())),
        VRoute("/health", _ => VResult.success(actuator.healthEndpoint()))
      )
    })
  }

  private def startServer(routes: List[VRoute]): VResult[VServer] = {
    VResult.fromTry(Try {
      val server = new VServer(config.serverHost, config.serverPort, routes)
      server.start() match {
        case VResult.Success(_) => server
        case VResult.Failure(error) => throw new RuntimeException(error.message)
      }
    })
  }

  // Helper for users who want to use services in simple routes (with error handling)
  protected def service[T](implicit classTag: ClassTag[T]): VResult[T] = {
    VResult.fromTry(Try(services.get[T]))
  }

  // Helper to access configuration
  protected def getConfig: AppConfiguration = config
}
