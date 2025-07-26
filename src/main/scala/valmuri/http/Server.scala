package valmuri.http

import zio._
import zio.http._
import valmuri.config.ServerConfig
import valmuri.routing.Router
import valmuri.http.HttpAppAdapter

trait Server {
  def start(): Task[Unit]
  def stop(): Task[Unit]
  def restart(): Task[Unit]
}

object Server {
  final case class ServerLive(
    config: ServerConfig,
    router: Router,
    serverRef: Ref[Option[Fiber.Runtime[Throwable, Nothing]]],
  ) extends Server {

    def start(): Task[Unit] = for {
      _      <- ZIO.logInfo(s"🚀 Starting server on ${config.host}:${config.port}")
      httpApp = HttpAppAdapter.fromRouter(router)
      fiber <- zio.http.Server
                 .serve(httpApp)
                 .provide(zio.http.Server.defaultWithPort(config.port))
                 .fork
      _ <- serverRef.set(Some(fiber))
      _ <- ZIO.logInfo(s"✅ Server started on http://${config.host}:${config.port}")
    } yield ()

    def stop(): Task[Unit] = for {
      _        <- ZIO.logInfo("🛑 Stopping server")
      fiberOpt <- serverRef.get
      _ <- fiberOpt match {
             case Some(fiber) => fiber.interrupt
             case None        => ZIO.unit
           }
      _ <- serverRef.set(None)
      _ <- ZIO.logInfo("✅ Server stopped")
    } yield ()

    def restart(): Task[Unit] = for {
      _ <- stop()
      _ <- start()
    } yield ()
  }

  val live: ZLayer[ServerConfig with Router, Nothing, Server] =
    ZLayer.fromZIO {
      for {
        config    <- ZIO.service[ServerConfig]
        router    <- ZIO.service[Router]
        serverRef <- Ref.make(Option.empty[Fiber.Runtime[Throwable, Nothing]])
      } yield ServerLive(config, router, serverRef)
    }
}
