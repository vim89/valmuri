package com.vitthalmirji.valmuri


import com.sun.net.httpserver.HttpServer
import com.vitthalmirji.valmuri.handler.{DefaultHandler, EnhancedVHandler}

import java.net.InetSocketAddress
import scala.util.Try

/**
 * Enhanced HTTP server with pattern matching and async support
 */
private[valmuri] class VServer(host: String, port: Int, routes: List[VRoute]) {

  // Type alias for cleaner code
  type ServerInstance = HttpServer

  private var serverInstance: Option[ServerInstance] = None

  def start(): VResult[Unit] = {
    VResult.fromTry(Try {
      val server = HttpServer.create(new InetSocketAddress(host, port), 0)

      // Register all routes with enhanced error handling
      routes.foreach(registerRoute(server, _))

      // Add default error handler
      server.createContext("/", new DefaultHandler(routes))

      server.setExecutor(null)
      server.start()
      serverInstance = Some(server)

      println(s"🌐 Server started on $host:$port")
    })
  }

  def stop(): VResult[Unit] = {
    VResult.fromTry(Try {
      serverInstance.foreach { server =>
        server.stop(0)
        println("🛑 Server stopped")
      }
      serverInstance = None
    })
  }

  private def registerRoute(server: ServerInstance, route: VRoute): Unit = {
    val _ = server.createContext(route.path, new EnhancedVHandler(route))
  }
}
