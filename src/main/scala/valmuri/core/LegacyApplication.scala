package valmuri.core

import valmuri.config.AppConfig
import zio._
import valmuri.routing.Router

@deprecated("Use ValmuriApplication instead", "0.2.0")
trait FlowApplication {
  def configure(): AppConfig
  def initialize(): Task[Unit] = ZIO.unit
  def shutdown(): Task[Unit] = ZIO.unit
}

object FlowRunner {
  @deprecated("Use ValmuriApplication.run instead", "0.2.0")
  def run[A <: FlowApplication](app: A): Task[ExitCode] = {
    ZIO.logWarning("⚠️ FlowApplication is deprecated. Use ValmuriApplication instead.") *>
      ZIO.succeed(ExitCode.success)
  }
}
