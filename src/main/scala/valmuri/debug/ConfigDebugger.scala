package valmuri.debug

import zio._
import valmuri.config.AppConfig

object ConfigDebugger {
  def printConfiguration(config: AppConfig): Task[Unit] = for {
    _ <- ZIO.logInfo("🔍 === VALMURI CONFIGURATION DEBUG ===")
    _ <- ZIO.logInfo(s"Server: ${config.server.host}:${config.server.port}")
    _ <- ZIO.logInfo(s"Database: ${config.database.url}")
    _ <- ZIO.logInfo(s"Logging: ${config.logging.level}")
    _ <- ZIO.logInfo(s"Features: ${config.features}")
    _ <- ZIO.logInfo("🔍 === END DEBUG ===")
  } yield ()

  def validateEnvironment(): Task[Unit] = for {
    env <- ZIO.attempt(sys.env.getOrElse("VALMURI_ENV", "development"))
    _   <- ZIO.logInfo(s"🌍 Environment: $env")
    _ <- ZIO.when(env == "production") {
           ZIO.logWarning("⚠️ Running in PRODUCTION mode")
         }
  } yield ()
}
