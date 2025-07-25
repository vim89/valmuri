package valmuri.config

import zio._
import zio.config.magnolia.DeriveConfig.deriveConfig

case class AppConfig(host: String, port: Int)

object AppConfigModule {
  // automatically derived descriptor
  private val configDescriptor: Config[AppConfig] = deriveConfig[AppConfig]

  val live: ZLayer[Any, Throwable, AppConfig] =
    ZLayer.fromZIO(ZIO.config(configDescriptor))

  def get: ZIO[AppConfig, Throwable, AppConfig] =
    ZIO.service[AppConfig]

  def host: ZIO[AppConfig, Throwable, String] =
    ZIO.serviceWith[AppConfig](_.host)

  def port: ZIO[AppConfig, Throwable, Int] =
    ZIO.serviceWith[AppConfig](_.port)
}
