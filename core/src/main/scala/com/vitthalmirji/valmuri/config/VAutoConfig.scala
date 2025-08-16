package com.vitthalmirji.valmuri.config

import com.vitthalmirji.valmuri.actuator.VActuator
import com.vitthalmirji.valmuri.config.types.VConfigExtended
import com.vitthalmirji.valmuri.di.VServices
import com.vitthalmirji.valmuri.error.VResult

import scala.util.Try

/**
 * Auto-configuration that works with existing VConfig
 */
class VAutoConfig(config: VConfig, services: VServices) {

  // Create enhanced config while preserving the original
  private lazy val enhancedConfig = VConfigExtended.fromVConfig(config)

  /**
   * Auto-configure framework components
   * This method integrates with your existing VApplication.initializeFramework()
   */
  def configure(): VResult[Unit] =
    for {
      _ <- configureCore()
      _ <- configureActuator()
      _ <- configureServices()
      _ <- validateConfiguration()
    } yield ()

  /**
   * Register core configuration objects
   * These become available for dependency injection
   */
  private def configureCore(): VResult[Unit] =
    for {
      _ <- services.register(config)         // Original VConfig for backward compatibility
      _ <- services.register(enhancedConfig) // Enhanced config for new features
      _ <- VResult.success(println("⚙️  Core configuration registered"))
    } yield ()

  /**
   * Auto-configure actuator if enabled
   * Integrates with your existing actuator routes in VApplication
   */
  private def configureActuator(): VResult[Unit] =
    if (enhancedConfig.actuator.enabled) {
      for {
        actuator <- VResult.fromTry(Try(new VActuator(enhancedConfig)))
        _        <- services.register(actuator)
        _        <- VResult.success(println(s"⚙️  Actuator configured: ${enhancedConfig.actuator.basePath}"))
      } yield ()
    } else {
      VResult.success(println("⚙️  Actuator disabled"))
    }

  /**
   * Register common services that framework components need
   */
  private def configureServices(): VResult[Unit] =
    for {
      // Register configuration sections as individual services
      _ <- services.register(enhancedConfig.server)
      _ <- services.register(enhancedConfig.cors)
      _ <- services.register(enhancedConfig.database)
      _ <- VResult.success(println("⚙️  Framework services configured"))
    } yield ()

  /**
   * Validate configuration for common issues
   * Helps catch configuration problems early
   */
  private def validateConfiguration(): VResult[Unit] =
    VResult.fromTry(Try {
      // Validate server settings
      if (!enhancedConfig.server.isValidPort) {
        throw new IllegalArgumentException(s"Invalid port: ${enhancedConfig.server.port}")
      }

      if (!enhancedConfig.server.isValidHost) {
        throw new IllegalArgumentException(s"Invalid host: ${enhancedConfig.server.host}")
      }

      // Validate database settings if provided
      enhancedConfig.database.url.foreach { url =>
        if (!url.startsWith("jdbc:")) {
          throw new IllegalArgumentException(s"Invalid database URL: $url")
        }
      }

      println("✅ Configuration validation passed")
    })

  /**
   * Get the enhanced configuration for use in other components
   */
  def getEnhancedConfig: VConfigExtended = enhancedConfig
}
