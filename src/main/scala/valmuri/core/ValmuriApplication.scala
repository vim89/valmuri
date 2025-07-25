package valmuri.core

import zio._
import valmuri.config.{AppConfig, ConfigLoader, DatabaseConfig, FeatureConfig, LoggingConfig, SecurityConfig, ServerConfig}
import valmuri.di.{Container, ServiceRegistry}
import valmuri.routing.Router

abstract class ValmuriApplication extends ZIOAppDefault {

  // Override these in your application
  def configure(): AppConfig   = AppConfig.default
  def routes(): Router         = Router()
  def initialize(): Task[Unit] = ZIO.unit
  def shutdown(): Task[Unit]   = ZIO.unit

  // Framework lifecycle
  final def run: ZIO[Any, Throwable, Unit] = {
    val app = for {
      _      <- ZIO.logInfo("🎯 Initializing Valmuri Framework")
      config <- loadConfiguration()
      _      <- runApplication(config)
    } yield ()

    app.provide(
      ConfigLoader.live,
      ServiceRegistry.live,
      Container.live,
      ApplicationContext.live,
    )
  }

  private def loadConfiguration(): ZIO[ConfigLoader, Throwable, AppConfig] = for {
    configLoader <- ZIO.service[ConfigLoader]
    baseConfig   <- configLoader.load()
    userConfig  = configure()
    finalConfig = mergeConfigs(baseConfig, userConfig)
  } yield finalConfig

  private def runApplication(
      config: AppConfig
  ): ZIO[Container with ApplicationContext, Throwable, Unit] = for {
    context <- ZIO.service[ApplicationContext]
    _       <- initialize()
    _       <- context.start()
    _       <- ZIO.addFinalizer(context.stop() *> shutdown())
    _       <- ZIO.never // Keep app running
  } yield ()

  private def mergeConfigs(base: AppConfig, user: AppConfig): AppConfig =
    // Intelligent config merging - user overrides take precedence
    base.copy(
      server = if (user.server != ServerConfig.default) user.server else base.server,
      database = if (user.database != DatabaseConfig.default) user.database else base.database,
      logging = if (user.logging != LoggingConfig.default) user.logging else base.logging,
      security = if (user.security != SecurityConfig.default) user.security else base.security,
      features = if (user.features != FeatureConfig.default) user.features else base.features,
    )
}
