package valmuri.config

import zio._
import zio.config.magnolia.DeriveConfig.deriveConfig

case class AppConfig2(host: String, port: Int)

object AppConfigModule {
  // automatically derived descriptor
  private val configDescriptor: Config[AppConfig] = deriveConfig[AppConfig]

  val live: ZLayer[Any, Throwable, AppConfig] =
    ZLayer.fromZIO(ZIO.config(configDescriptor))

  def get: ZIO[AppConfig2, Throwable, AppConfig2] =
    ZIO.service[AppConfig2]

  def host: ZIO[AppConfig2, Throwable, String] =
    ZIO.serviceWith[AppConfig2](_.host)

  def port: ZIO[AppConfig2, Throwable, Int] =
    ZIO.serviceWith[AppConfig2](_.port)
}
