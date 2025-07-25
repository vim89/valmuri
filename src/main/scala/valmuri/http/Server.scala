package valmuri.http

import valmuri.routing.Router
import zio.{ZIO, ZIOAppDefault}

object Server extends ZIOAppDefault {
  def run: ZIO[Any, Throwable, Nothing] = {
    val router = Router.apply() // Initialize your router with routes

    // Create the HTTP app from the router
    val httpApp = HttpAppAdapter.fromRouter(router)

    // Start the server
    zio.http.Server.serve(httpApp).provide(
      zio.http.Server.defaultWithPort(8080), // Default port
    )
  }
}
