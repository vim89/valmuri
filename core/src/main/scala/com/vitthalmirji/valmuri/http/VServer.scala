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

      // Register (literal) specific routes first, in descending path length—this ensures longest prefix matches
      val specificRoutes = routes.filterNot(_.path.endsWith("/*")).sortBy(_.path.length)(Ordering[Int].reverse)
      specificRoutes.foreach { route =>
        routeRegistry(route.path) = route
        println(s"[valmuri] Registering specific route: ${route.path}")
        serverCreateContext(route.path, Some(httpServer), new VHttpHandler(route, config))
      }

      // For routes ending in '/*', register them as prefix routes without the wildcard
      val prefixRoutes = routes.filter(_.path.endsWith("/*"))
      prefixRoutes.foreach { route =>
        val prefix = route.path.stripSuffix("/*") + "/"
        routeRegistry(prefix) = route
        println(s"[valmuri] Registering prefix route: $prefix (from wildcard ${route.path})")
        serverCreateContext(prefix, Some(httpServer), new VHttpHandler(route, config))
      }

      // Finally, register the root fallback context for unmatched paths
      println("[valmuri] Registering default handler for '/'")
      serverCreateContext("/", Some(httpServer), new DefaultHandler(routeRegistry.values.toList))

      // Start server
      httpServer.start()

      sys.addShutdownHook {
        println("[valmuri] JVM shutdown hook: stopping HTTP server")
        httpServer.stop(config.serverShutdownDelay)
        threadPool.shutdown()
        if (!threadPool.awaitTermination(config.serverShutdownDelay.toLong, TimeUnit.SECONDS)) {
          threadPool.shutdownNow()
        }
      }

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
