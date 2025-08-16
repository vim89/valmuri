package com.vitthalmirji.valmuri.config.types

import com.vitthalmirji.valmuri.config.VConfig

/**
 * Enhanced configuration with type-safe access
 * Works with your existing VConfig - no breaking changes
 */
case class VConfigExtended(
  base: VConfig,
  server: ServerSettings,
  actuator: ActuatorSettings,
  cors: CorsSettings,
  database: DatabaseSettings
) {
  // Delegate all existing VConfig methods to preserve compatibility
  def appName: String             = base.appName
  def appVersion: String          = base.appVersion
  def profile: String             = base.profile
  def serverHost: String          = base.serverHost
  def serverPort: Int             = base.serverPort
  def actuatorEnabled: Boolean    = base.actuatorEnabled
  def staticDir: Option[String]   = base.staticDir
  def templateDir: Option[String] = base.templateDir
  def uploadDir: Option[String]   = base.uploadDir
  def custom: Map[String, String] = base.custom
}

object VConfigExtended {

  /**
   * Create enhanced config from your existing VConfig
   * This ensures full backward compatibility
   */
  def fromVConfig(config: VConfig): VConfigExtended =
    VConfigExtended(
      base = config,
      server = ServerSettings(
        host = config.serverHost,
        port = config.serverPort,
        threads = config.serverThreads,
        backlog = config.serverBacklog,
        shutdownDelay = config.serverShutdownDelay,
        maxRequestSize = config.maxRequestSize
      ),
      actuator = ActuatorSettings(
        enabled = config.actuatorEnabled,
        basePath = "/actuator" // Default path
      ),
      cors = CorsSettings(
        enabled = config.corsEnabled,
        origin = config.corsOrigin
      ),
      database = DatabaseSettings(
        url = config.databaseUrl,
        driver = config.databaseDriver,
        username = config.databaseUsername,
        password = config.databasePassword
      )
    )
}
