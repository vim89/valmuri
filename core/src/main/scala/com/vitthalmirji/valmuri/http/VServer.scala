package com.vitthalmirji.valmuri.http

import com.sun.net.httpserver.HttpServer
import com.vitthalmirji.valmuri.config.VConfig
import com.vitthalmirji.valmuri.error.VResult
import com.vitthalmirji.valmuri.handler.{ DefaultHandler, VHttpHandler }

import java.net.InetSocketAddress
import java.util.concurrent.{ Executors, ThreadPoolExecutor, TimeUnit }
import scala.collection.mutable
import scala.util.Try

/**
 * Production-ready HTTP server
 */
class VServer(config: VConfig) {

  private var server: Option[HttpServer]           = None
  private var executor: Option[ThreadPoolExecutor] = None
  private val routeRegistry                        = mutable.Map[String, VRoute]()

  def start(routes: List[VRoute]): VResult[Unit] =
    VResult.fromTry(Try {
      // Create thread pool
      val threadPool = Executors
        .newFixedThreadPool(
          config.serverThreads,
          (r: Runnable) => {
            val t = new Thread(r, s"valmuri-http-${Thread.activeCount()}")
            t.setDaemon(true)
            t
          }
        )
        .asInstanceOf[ThreadPoolExecutor]

      // Create HTTP server
      val httpServer = HttpServer.create(
        new InetSocketAddress(config.serverHost, config.serverPort),
        config.serverBacklog
      )

      // Configure server
      httpServer.setExecutor(threadPool)

      // Register routes
      routes.foreach { route =>
        routeRegistry(route.path) = route
        serverCreateContext(route.path, server, new VHttpHandler(route, config))
      }

      // Add default handler for 404
      serverCreateContext("/", server, new DefaultHandler(routeRegistry.values.toList))

      // Start server
      httpServer.start()

      server = Some(httpServer)
      executor = Some(threadPool)
    })

  def stop(): VResult[Unit] =
    VResult.fromTry(Try {
      server.foreach(s => s.stop(config.serverShutdownDelay))

      executor.foreach { e =>
        e.shutdown()
        if (!e.awaitTermination(config.serverShutdownDelay.toLong, TimeUnit.SECONDS)) {
          e.shutdownNow()
        }
      }

      server = None
      executor = None
    })

  private def serverCreateContext(path: String, server: Option[HttpServer], handler: VHttpHandler) =
    try
      server.map(_.createContext(path, handler))
    catch {
      case iae: IllegalArgumentException if iae.getMessage.contains("cannot add context to list") =>
        println("[valmuri] context \"/\" already registered, skipping")
    }

  private def serverCreateContext(path: String, server: Option[HttpServer], handler: DefaultHandler) =
    try
      server.map(_.createContext(path, handler))
    catch {
      case iae: IllegalArgumentException if iae.getMessage.contains("cannot add context to list") =>
        println("[valmuri] context \"/\" already registered, skipping")
    }
}
