package valmuri.config

import zio._

trait ConfigCache {
  def get(): Task[AppConfig]
  def invalidate(): Task[Unit]
  def refresh(): Task[AppConfig]
}

object ConfigCache {
  final case class ConfigCacheLive(
                                    cache: Ref[Option[AppConfig]],
                                    loader: ConfigLoader
                                  ) extends ConfigCache {

    def get(): Task[AppConfig] = for {
      cached <- cache.get
      config <- cached match {
        case Some(config) => ZIO.succeed(config)
        case None => refresh()
      }
    } yield config

    def invalidate(): Task[Unit] = cache.set(None)

    def refresh(): Task[AppConfig] = for {
      config <- loader.load()
      _ <- cache.set(Some(config))
    } yield config
  }

  val live: ZLayer[ConfigLoader, Nothing, ConfigCache] =
    ZLayer.fromZIO {
      for {
        cache <- Ref.make(Option.empty[AppConfig])
        loader <- ZIO.service[ConfigLoader]
      } yield ConfigCacheLive(cache, loader)
    }
}
