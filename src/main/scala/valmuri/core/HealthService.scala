package valmuri.core

import zio._

trait HealthService {
  def health: UIO[String]
}

object HealthService {
  val live: ZLayer[Any, Nothing, HealthService] =
    ZLayer.succeed(
      new HealthService {
        def health: UIO[String] = ZIO.succeed("OK")
      }
    )
}

object HealthServiceLive {
  def health: URIO[HealthService, String] =
    ZIO.serviceWithZIO[HealthService](_.health)

  def live: ZLayer[Any, Nothing, HealthService] =
    HealthService.live
}
