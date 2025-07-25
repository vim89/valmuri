package valmuri.core

import valmuri.config.AppConfig
import valmuri.di.Container
import zio._

trait ApplicationContext {
  def start(): Task[Unit]
  def stop(): Task[Unit]
  def restart(): Task[Unit]
  def getConfig(): Task[AppConfig]
  def getContainer(): Task[Container]
}

object ApplicationContext {
  final case class ApplicationContextLive(
      config: AppConfig,
      container: Container,
      server: Server,
  ) extends ApplicationContext {

    def start(): Task[Unit] = for {
      _ <- ZIO.logInfo("🚀 Starting Valmuri Application")
      _ <- container.start()
      _ <- server.start()
      _ <- ZIO.logInfo(s"✅ Valmuri running on http://${config.server.host}:${config.server.port}")
    } yield ()

    def stop(): Task[Unit] = for {
      _ <- ZIO.logInfo("🛑 Stopping Valmuri Application")
      _ <- server.stop()
      _ <- container.stop()
      _ <- ZIO.logInfo("✅ Valmuri stopped gracefully")
    } yield ()

    def restart(): Task[Unit] = for {
      _ <- stop()
      _ <- start()
    } yield ()

    def getConfig(): Task[AppConfig]    = ZIO.succeed(config)
    def getContainer(): Task[Container] = ZIO.succeed(container)
  }

  val live: ZLayer[AppConfig with Container with Server, Nothing, ApplicationContext] =
    ZLayer.fromZIO {
      for {
        config    <- ZIO.service[AppConfig]
        container <- ZIO.service[Container]
        server    <- ZIO.service[Server]
      } yield ApplicationContextLive(config, container, server)
    }
}
