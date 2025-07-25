package valmuri.config

import zio._
import zio.config._
import zio.config.magnolia.DeriveConfig

// Core application configuration
case class AppConfig(
    server: ServerConfig = ServerConfig.default,
    database: DatabaseConfig = DatabaseConfig.default,
    logging: LoggingConfig = LoggingConfig.default,
    security: SecurityConfig = SecurityConfig.default,
    features: FeatureConfig = FeatureConfig.default,
)

case class ServerConfig(
    host: String = "localhost",
    port: Int = 8080,
    maxConnections: Int = 1000,
    requestTimeout: Duration = 30.seconds,
    shutdownTimeout: Duration = 10.seconds,
)

case class DatabaseConfig(
    driver: String = "org.sqlite.JDBC",
    url: String = "jdbc:sqlite:./valmuri.db",
    username: Option[String] = None,
    password: Option[String] = None,
    poolSize: Int = 10,
    connectionTimeout: Duration = 5.seconds,
    migrations: MigrationConfig = MigrationConfig.default,
)

case class MigrationConfig(
    enabled: Boolean = true,
    location: String = "db/migrations",
    autoMigrate: Boolean = true,
)

case class LoggingConfig(
    level: String = "INFO",
    format: String = "text", // text | json
    file: Option[String] = None,
    console: Boolean = true,
)

case class SecurityConfig(
    cors: CorsConfig = CorsConfig.default,
    csrf: CsrfConfig = CsrfConfig.default,
    headers: SecurityHeadersConfig = SecurityHeadersConfig.default,
)

case class CorsConfig(
    enabled: Boolean = true,
    allowedOrigins: List[String] = List("*"),
    allowedMethods: List[String] = List("GET", "POST", "PUT", "DELETE", "OPTIONS"),
    allowedHeaders: List[String] = List("*"),
)

case class CsrfConfig(
    enabled: Boolean = true,
    cookieName: String = "valmuri-csrf-token",
    headerName: String = "X-CSRF-Token",
)

case class SecurityHeadersConfig(
    enabled: Boolean = true,
    contentTypeOptions: Boolean = true,
    frameOptions: String = "DENY",
    xssProtection: Boolean = true,
)

case class FeatureConfig(
    adminPanel: Boolean = false,
    apiDocs: Boolean = true,
    metrics: Boolean = true,
    healthCheck: Boolean = true,
)

object AppConfig {
  implicit val config: Config[AppConfig] = DeriveConfig.deriveConfig[AppConfig]

  val default: AppConfig = AppConfig()
}

// Companion objects with defaults
object ServerConfig {
  val default: ServerConfig                 = ServerConfig()
  implicit val config: Config[ServerConfig] = DeriveConfig.deriveConfig[ServerConfig]
}

object DatabaseConfig {
  val default: DatabaseConfig                 = DatabaseConfig()
  implicit val config: Config[DatabaseConfig] = DeriveConfig.deriveConfig[DatabaseConfig]
}

object LoggingConfig {
  val default: LoggingConfig                 = LoggingConfig()
  implicit val config: Config[LoggingConfig] = DeriveConfig.deriveConfig[LoggingConfig]
}

object SecurityConfig {
  val default: SecurityConfig                 = SecurityConfig()
  implicit val config: Config[SecurityConfig] = DeriveConfig.deriveConfig[SecurityConfig]
}

object FeatureConfig {
  val default: FeatureConfig                 = FeatureConfig()
  implicit val config: Config[FeatureConfig] = DeriveConfig.deriveConfig[FeatureConfig]
}

object CorsConfig {
  val default: CorsConfig                 = CorsConfig()
  implicit val config: Config[CorsConfig] = DeriveConfig.deriveConfig[CorsConfig]
}

object CsrfConfig {
  val default: CsrfConfig                 = CsrfConfig()
  implicit val config: Config[CsrfConfig] = DeriveConfig.deriveConfig[CsrfConfig]
}

object SecurityHeadersConfig {
  val default: SecurityHeadersConfig                 = SecurityHeadersConfig()
  implicit val config: Config[SecurityHeadersConfig] =
    DeriveConfig.deriveConfig[SecurityHeadersConfig]
}

object MigrationConfig {
  val default: MigrationConfig                 = MigrationConfig()
  implicit val config: Config[MigrationConfig] = DeriveConfig.deriveConfig[MigrationConfig]
}
