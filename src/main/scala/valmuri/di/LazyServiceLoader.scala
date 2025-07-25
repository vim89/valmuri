package valmuri.di

import zio._

trait LazyServiceLoader {
  def loadOnDemand[A: Tag](factory: () => A): ZLayer[Any, Nothing, A]
}

object LazyServiceLoader {
  final case class LazyServiceLoaderLive() extends LazyServiceLoader {
    def loadOnDemand[A: Tag](factory: () => A): ZLayer[Any, Nothing, A] =
      ZLayer.fromZIO(ZIO.succeed(factory()))
  }

  val live: ZLayer[Any, Nothing, LazyServiceLoader] =
    ZLayer.succeed(LazyServiceLoaderLive())
}
