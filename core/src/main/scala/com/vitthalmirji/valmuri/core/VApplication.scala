package com.vitthalmirji.valmuri.core

import com.vitthalmirji.valmuri.config.{ VAutoConfig, VConfig }
import com.vitthalmirji.valmuri.di.VServices
import com.vitthalmirji.valmuri.error.VResult
import com.vitthalmirji.valmuri.http.{ VRoute, VServer }
import com.vitthalmirji.valmuri.error.ValmuriError.RoutingError

import scala.reflect.ClassTag
import scala.util.Try

/**
 * Core framework trait - Production ready
 */
trait VApplication {

  // Lazy initialization for better startup performance
  private lazy val services: VServices     = new VServices()
  protected lazy val config: VConfig       = loadConfiguration()
  private lazy val autoConfig: VAutoConfig = new VAutoConfig(config, services)

  // User overrides
  def routes(): List[VRoute] = List.empty

  def configure(): VResult[Unit] = VResult.success(())

  // Main entry point
  final def start(): VResult[Unit] = {
    val startTime = System.currentTimeMillis()

    for {
      _      <- printBanner()
      _      <- initializeFramework()
      _      <- configure()
      routes <- buildAllRoutes()
      _      <- startServer(routes)
      _      <- printStartupInfo(System.currentTimeMillis() - startTime)
    } yield ()
  }

  private def printBanner(): VResult[Unit] = VResult.success {
    println("""
        |╦  ╦┌─┐┬  ┌┬┐┬ ┬┬─┐┬
        |╚╗╔╝├─┤│  ││││ │├┬┘│
        | ╚╝ ┴ ┴┴─┘┴ ┴└─┘┴└─┴
        |Full-stack scala framework v0.1.0
    """.stripMargin)
  }

  private def initializeFramework(): VResult[Unit] =
    VResult.fromTry(Try {
      autoConfig.configure()
      println(s"✅ Framework initialized with profile: ${config.profile}")
    })

  private def buildAllRoutes(): VResult[List[VRoute]] =
    for {
      userRoutes     <- VResult.success(routes())
      staticRoutes   <- buildStaticRoutes()
      actuatorRoutes <- buildActuatorRoutes()
    } yield userRoutes ++ staticRoutes ++ actuatorRoutes

  private def buildStaticRoutes(): VResult[List[VRoute]] =
    if (config.staticDir.isDefined) {
      VResult.success(
        List(VRoute("/static/*", _ => VResult.fromOption(config.staticDir, RoutingError("Invalid route"))))
      )
    } else {
      VResult.success(List.empty)
    }

  private def buildActuatorRoutes(): VResult[List[VRoute]] =
    if (config.actuatorEnabled) {
      VResult.success(
        List(
          VRoute("/actuator/health", _ => VResult.success(healthCheck())),
          VRoute("/actuator/metrics", _ => VResult.success(metrics())),
          VRoute("/actuator/info", _ => VResult.success(info()))
        )
      )
    } else {
      VResult.success(List.empty)
    }

  private def startServer(routes: List[VRoute]): VResult[VServer] = {
    val server = new VServer(config)
    server.start(routes).map(_ => server)
  }

  private def printStartupInfo(startupTime: Long): VResult[Unit] = VResult.success {
    println(s"""
         |✅ ${config.appName} started in ${startupTime}ms
         |🌐 Server: http://${config.serverHost}:${config.serverPort}
         |📊 Health: http://${config.serverHost}:${config.serverPort}/actuator/health
         |🚀 Environment: ${config.profile}
    """.stripMargin)
  }

  private def loadConfiguration(): VConfig =
    VConfig.load()

  // Health check endpoint
  private def healthCheck(): String =
    s"""{"status":"UP","timestamp":"${java.time.Instant.now()}"}"""

  // Metrics endpoint
  private def metrics(): String = {
    val runtime = Runtime.getRuntime
    s"""{
      "memory": {
        "used": ${(runtime.totalMemory() - runtime.freeMemory()) / 1048576},
        "total": ${runtime.totalMemory() / 1048576},
        "max": ${runtime.maxMemory() / 1048576}
      },
      "processors": ${runtime.availableProcessors()}
    }"""
  }

  // Info endpoint
  private def info(): String =
    s"""{
      "app": "${config.appName}",
      "version": "${config.appVersion}",
      "framework": "Valmuri 0.1.0"
    }"""

  // Helper methods for users
  protected def service[T: ClassTag]: T = services.get[T]

  protected def getConfig: VConfig = config
}
