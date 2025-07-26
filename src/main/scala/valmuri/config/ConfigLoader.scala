package valmuri.config

import zio._
import zio.config._
import zio.config.typesafe._
import zio.config.magnolia.DeriveConfig
import com.typesafe.config.ConfigFactory
import java.io.File

trait ConfigLoader {
  def load(): Task[AppConfig]
  def loadFromFile(path: String): Task[AppConfig]
  def loadFromEnvironment(): Task[AppConfig]
  def reload(): Task[AppConfig]
}

object ConfigLoader {
  final private case class ConfigLoaderLive() extends ConfigLoader {

    def load(): Task[AppConfig] = for {
      _          <- ZIO.logInfo("📋 Loading Valmuri configuration")
      environment = sys.env.getOrElse("VALMURI_ENV", "development")
      _          <- ZIO.logInfo(s"🌍 Environment: $environment")
      config     <- loadConfiguration(environment)
      _          <- validateConfiguration(config)
      _          <- ZIO.logInfo("✅ Configuration loaded successfully")
    } yield config

    def loadFromFile(path: String): Task[AppConfig] = for {
      _         <- ZIO.logInfo(s"📁 Loading config from file: $path")
      config    <- ZIO.attempt(ConfigFactory.parseFile(new File(path)))
      provider   = ConfigProvider.fromTypesafeConfig(config)
      appConfig <- ZIO.config(AppConfig.config).provideLayer(ZLayer.succeed(provider))
    } yield appConfig

    def loadFromEnvironment(): Task[AppConfig] = for {
      _ <- ZIO.logInfo("🌐 Loading config from environment variables")
      envConfig <- ZIO
                     .config(AppConfig.config)
                     .provideLayer(ZLayer.succeed(ConfigProvider.envProvider))
    } yield envConfig

    def reload(): Task[AppConfig] = for {
      _      <- ZIO.logInfo("🔄 Reloading configuration")
      config <- load()
    } yield config

    private def loadConfiguration(environment: String): Task[AppConfig] = {
      val configFiles = List(
        s"application-$environment.conf",
        "application.conf",
        "reference.conf",
      )

      for {
        hoconConfig <- ZIO.attempt {
                         val configs = configFiles.map { filename =>
                           val resource = getClass.getClassLoader.getResource(filename)
                           if (resource != null) {
                             ConfigFactory.parseResources(filename)
                           } else {
                             ConfigFactory.empty()
                           }
                         }
                         configs
                           .foldLeft(ConfigFactory.empty())(_ withFallback _)
                           .withFallback(ConfigFactory.systemEnvironment())
                           .withFallback(ConfigFactory.systemProperties())
                           .resolve()
                       }

        provider   = ConfigProvider.fromTypesafeConfig(hoconConfig)
        appConfig <- ZIO.config(AppConfig.config).provideLayer(ZLayer.succeed(provider))

      } yield appConfig
    }

    private def validateConfiguration(config: AppConfig): Task[Unit] = for {
      _ <- ZIO.when(config.server.port < 1 || config.server.port > 65535) {
             ZIO.fail(new IllegalArgumentException(s"Invalid port: ${config.server.port}"))
           }
      _ <- ZIO.when(config.database.poolSize < 1) {
             ZIO.fail(new IllegalArgumentException(s"Invalid pool size: ${config.database.poolSize}"))
           }
      _ <- ZIO.logInfo("✅ Configuration validation passed")
    } yield ()
  }

  val live: ZLayer[Any, Nothing, ConfigLoader] =
    ZLayer.succeed(ConfigLoaderLive())

  val configLayer: ZLayer[Any, Throwable, AppConfig] =
    ZLayer.fromZIO {
      ConfigLoaderLive().load()
    }
}
