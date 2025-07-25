package valmuri.hello

import zio._
import valmuri.core.ValmuriApplication
import valmuri.config.{AppConfig, ServerConfig}
import valmuri.routing.Router

object HelloWorldApp extends ValmuriApplication {

  override def configure(): AppConfig = AppConfig.default.copy(
    server = ServerConfig(port = 8080, host = "0.0.0.0")
  )

  override def routes(): Router = AppRoutes.router

  override def initialize(): Task[Unit] = for {
    _ <- ZIO.logInfo("🎯 Initializing Hello World App")
    _ <- ZIO.logInfo("📝 Custom initialization logic here")
  } yield ()

  override def shutdown(): Task[Unit] = for {
    _ <- ZIO.logInfo("👋 Hello World App shutting down")
    _ <- ZIO.logInfo("🧹 Cleanup logic here")
  } yield ()
}
