package valmuri.di

import zio._
import valmuri.config.{AppConfig, ConfigLoader}

trait Container {
  def start(): Task[Unit]
  def stop(): Task[Unit]
  def resolve[A: Tag]: Task[A]
  def register[A: Tag](instance: A): Task[Unit]
  def registerLayer[A: Tag](layer: ZLayer[Any, Throwable, A]): Task[Unit]
}

object Container {
  final case class ContainerLive(
                                  registry: ServiceRegistry,
                                  config: AppConfig,
                                  runtime: Ref[Map[String, Any]]
                                ) extends Container {

    def start(): Task[Unit] = for {
      _ <- ZIO.logInfo("🚀 Starting Valmuri DI Container")
      _ <- autoDiscoverServices()
      _ <- ZIO.logInfo("✅ DI Container started successfully")
    } yield ()

    def stop(): Task[Unit] = for {
      _ <- ZIO.logInfo("🛑 Stopping Valmuri DI Container")
      _ <- runtime.set(Map.empty)
      _ <- ZIO.logInfo("✅ DI Container stopped")
    } yield ()

    def resolve[A: Tag]: Task[A] = {
      val className = implicitly[Tag[A]].tag.shortName
      for {
        runtimeMap <- runtime.get
        instance <- ZIO.fromOption(runtimeMap.get(className))
          .orElseFail(new RuntimeException(s"Service not registered: $className"))
      } yield instance.asInstanceOf[A]
    }

    def register[A: Tag](instance: A): Task[Unit] = {
      val className = implicitly[Tag[A]].tag.shortName
      runtime.update(_ + (className -> instance))
    }

    def registerLayer[A: Tag](layer: ZLayer[Any, Throwable, A]): Task[Unit] = {
      val className = implicitly[Tag[A]].tag.shortName
      for {
        service <- layer.build.map(_.get).provide(ZLayer.succeed(()))
        _ <- register(service)
      } yield ()
    }

    private def autoDiscoverServices(): Task[Unit] = {
      // Auto-discovery will scan classpath for @ValmuriService annotations
      // For MVP, we'll register known framework services manually
      for {
        _ <- registerFrameworkServices()
        _ <- ZIO.logInfo("🔍 Auto-discovery completed")
      } yield ()
    }

    private def registerFrameworkServices(): Task[Unit] = for {
      _ <- registry.register("HealthService", valmuri.core.HealthService.live)
      _ <- registry.register("ConfigLoader", ConfigLoader.live)
      _ <- ZIO.logInfo("📦 Core framework services registered")
    } yield ()
  }

  val live: ZLayer[AppConfig with ServiceRegistry, Nothing, Container] =
    ZLayer.fromZIO {
      for {
        registry <- ZIO.service[ServiceRegistry]
        config <- ZIO.service[AppConfig]
        runtime <- Ref.make(Map.empty[String, Any])
      } yield ContainerLive(registry, config, runtime)
    }
}
