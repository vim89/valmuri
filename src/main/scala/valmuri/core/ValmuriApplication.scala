package valmuri.core

import zio._
import valmuri.config.{
  AppConfig,
  ConfigLoader,
  DatabaseConfig,
  FeatureConfig,
  LoggingConfig,
  SecurityConfig,
  ServerConfig,
}
import valmuri.di.{ Container, ServiceRegistry }
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
      router = routes()
      _ <- runApplication(config, router)
    } yield ()

    app.provide(
      ConfigLoader.configLayer,
      ServiceRegistry.live,
      Container.live,
      ZLayer.succeed(routes()),
      ZLayer.fromZIO(ZIO.service[AppConfig].map(_.server)),
      Server.live,
      ApplicationContext.live,
    )
  }

  private def loadConfiguration(): ZIO[AppConfig, Throwable, AppConfig] = for {
    baseConfig <- ZIO.service[AppConfig]
    userConfig  = configure()
    finalConfig = mergeConfigs(baseConfig, userConfig)
  } yield finalConfig

  private def runApplication(
      config: AppConfig,
      router: Router,
  ): ZIO[Container with ApplicationContext, Throwable, Unit] = ZIO.scoped {
    for {
      context <- ZIO.service[ApplicationContext]
      _       <- initialize()
      _       <- context.start()
      _       <- ZIO.addFinalizer(context.stop().ignore *> shutdown().ignore)
      _       <- ZIO.never // Keep app running
    } yield ()
  }

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
