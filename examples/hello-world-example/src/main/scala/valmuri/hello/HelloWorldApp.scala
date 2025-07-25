package valmuri.hello

import zio._
import valmuri.http.Server

object HelloWorldApp extends ZIOAppDefault {
  def run: ZIO[Any, Throwable, Unit] =
    Server.start(AppRoutes.router)
}
