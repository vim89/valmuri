package hello

import com.vitthalmirji.valmuri.core.VApplicationEnhanced
import com.vitthalmirji.valmuri.http.VRoute
import com.vitthalmirji.valmuri.error.VResult

// Example service for dependency injection
trait GreetingService {
  def greet(name: String): String
  def getStats(): String
}

class GreetingServiceImpl extends GreetingService {
  private var greetCount = 0

  def greet(name: String): String = {
    greetCount += 1
    s"Hello, $name! (Greeting #$greetCount from Enhanced Valmuri Framework)"
  }

  def getStats(): String = s"""{"totalGreetings": $greetCount}"""
}

/**
 * Enhanced application example that shows new features
 * while maintaining compatibility with existing VApplication
 */
object EnhancedHelloApp extends VApplicationEnhanced {

  /**
   * Configure services using enhanced DI features
   * This works alongside your existing configure() method
   */
  override def configure(): VResult[Unit] =
    for {
      // Use enhanced service registration with lifecycle tracking
      _ <- registerServiceWithLifecycle[GreetingService](new GreetingServiceImpl())
      _ <- VResult.success(println("✅ Enhanced services configured"))
    } yield ()

  /**
   * Define routes using both existing and enhanced features
   */
  override def routes(): List[VRoute] = List(
    // Simple routes (existing functionality)
    VRoute("/", _ => VResult.success("🚀 Enhanced Valmuri Framework!")),

    // Routes using dependency injection
    VRoute(
      "/greet/:name",
      req => {
        val name = req.params.getOrElse("name", "Anonymous")
        serviceWithLifecycle[GreetingService] match {
          case VResult.Success(greetingService) => VResult.success(greetingService.greet(name))
          case VResult.Failure(error)           => VResult.success(s"Error: ${error.message}")
        }
      }
    ),

    // Route showing enhanced configuration
    VRoute(
      "/config",
      _ => {
        val config = getEnhancedConfig
        VResult.success(s"""{
        "application": "${config.appName}",
        "version": "${config.appVersion}",
        "profile": "${config.profile}",
        "server": {
          "host": "${config.server.host}",
          "port": ${config.server.port},
          "threads": ${config.server.threads}
        },
        "features": {
          "actuator": ${config.actuator.enabled},
          "cors": ${config.cors.enabled}
        }
      }""")
      }
    ),

    // Route showing service stats
    VRoute(
      "/stats",
      _ =>
        serviceWithLifecycle[GreetingService] match {
          case VResult.Success(greetingService) => VResult.success(greetingService.getStats())
          case VResult.Failure(error)           => VResult.success(s"""{"error": "${error.message}"}""")
        }
    ),

    // Debug route for development
    VRoute(
      "/debug/services",
      _ => {
        debugServices()
        VResult.success("Service debug info printed to console - check your logs")
      }
    )
  )
}

object EnhancedMain {
  def main(args: Array[String]): Unit =
    EnhancedHelloApp.start() match {
      case VResult.Success(_) => println("✅ Enhanced application started successfully")
      case VResult.Failure(error) =>
        println(s"❌ Failed to start application: ${error.message}")
        System.exit(1)
    }
}
