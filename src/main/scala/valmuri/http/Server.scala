package valmuri.http

import valmuri.routing.Router
import zio._
import zio.http._

object Server {
  def start(router: Router): ZIO[Any, Throwable, Nothing] = {
    val app = HttpAppAdapter.fromRouter(router)

    Server
      .serve(app)
      .provide(Server.defaultWithPort(8080))
      .exitCode
      .flatMap(_ => ZIO.never)
  }
}
