package com.vitthalmirji.valmuri

import com.sun.net.httpserver.{ HttpExchange, HttpHandler, HttpServer }
import com.vitthalmirji.valmuri.config.VConfig
import com.vitthalmirji.valmuri.error.FrameworkError

import java.io.InputStream
import java.net.InetSocketAddress
import java.util.concurrent.{ Executors, ThreadPoolExecutor, TimeUnit }
import scala.collection.mutable
import scala.jdk.CollectionConverters._
import scala.util.{ Try, Using }

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
        httpServer.createContext(route.path, new VHttpHandler(route, config))
      }

      // Add default handler for 404
      httpServer.createContext("/", new DefaultHandler(routeRegistry.keys.toList))

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
}

/**
 * HTTP request handler
 */
class VHttpHandler(route: VRoute, config: VConfig) extends HttpHandler {

  override def handle(exchange: HttpExchange): Unit =
    try {
      // Parse request
      val request = parseRequest(exchange)

      // Handle request
      val result = route.handler(request)

      // Send response
      result match {
        case VResult.Success(content) =>
          sendResponse(exchange, 200, content)

        case VResult.Failure(error) =>
          val (status, message) = error match {
            case FrameworkError.NotFound(_)     => (404, error.message)
            case FrameworkError.BadRequest(_)   => (400, error.message)
            case FrameworkError.Unauthorized(_) => (401, error.message)
            case FrameworkError.Forbidden(_)    => (403, error.message)
            case _                              => (500, "Internal Server Error")
          }
          sendResponse(exchange, status, errorJson(message))
      }
    } catch {
      case ex: Exception =>
        sendResponse(exchange, 500, errorJson(ex.getMessage))
    }

  private def parseRequest(exchange: HttpExchange): VRequest = {
    val method = exchange.getRequestMethod
    val uri    = exchange.getRequestURI
    val path   = uri.getPath

    // Parse query parameters
    val queryParams = Option(uri.getQuery).fold(Map.empty[String, String]) { query =>
      query
        .split("&")
        .map { param =>
          val parts = param.split("=", 2)
          if (parts.length == 2) {
            parts(0) -> java.net.URLDecoder.decode(parts(1), "UTF-8")
          } else {
            parts(0) -> ""
          }
        }
        .toMap
    }

    // Parse headers
    val headers = exchange.getRequestHeaders.asScala.map { case (k, v) =>
      k -> v.asScala.mkString(",")
    }.toMap

    // Parse body (if present)
    val body = if (Set("POST", "PUT", "PATCH").contains(method)) {
      readBody(exchange.getRequestBody, config.maxRequestSize)
    } else {
      None
    }

    // Parse path parameters (simplified - real implementation would use regex)
    val pathParams = extractPathParams(route.path, path)

    VRequest(
      method = method,
      path = path,
      queryParams = queryParams,
      pathParams = pathParams,
      headers = headers,
      body = body
    )
  }

  private def readBody(input: InputStream, maxSize: Int): Option[String] =
    Using.resource(input) { stream =>
      val bytes = stream.readNBytes(maxSize)
      if (bytes.nonEmpty) Some(new String(bytes, "UTF-8")) else None
    }

  private def extractPathParams(pattern: String, actual: String): Map[String, String] = {
    // Simple implementation - real one would use regex
    // Example: /users/:id -> /users/123 => Map("id" -> "123")

    val patternParts = pattern.split("/").filter(_.nonEmpty)
    val actualParts  = actual.split("/").filter(_.nonEmpty)

    if (patternParts.length != actualParts.length) {
      Map.empty
    } else {
      patternParts
        .zip(actualParts)
        .collect {
          case (p, a) if p.startsWith(":") =>
            p.substring(1) -> a
        }
        .toMap
    }
  }

  private def sendResponse(exchange: HttpExchange, status: Int, content: String): Unit = {
    val bytes = content.getBytes("UTF-8")

    // Set headers
    exchange.getResponseHeaders.add("Content-Type", detectContentType(content))
    exchange.getResponseHeaders.add("Server", "Valmuri/0.1.0")

    // Add CORS headers if configured
    if (config.corsEnabled) {
      exchange.getResponseHeaders.add("Access-Control-Allow-Origin", config.corsOrigin)
      exchange.getResponseHeaders.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
      exchange.getResponseHeaders.add("Access-Control-Allow-Headers", "Content-Type, Authorization")
    }

    // Send response
    exchange.sendResponseHeaders(status, bytes.length.toLong)

    Using.resource(exchange.getResponseBody) { output =>
      output.write(bytes)
      output.flush()
    }
  }

  private def detectContentType(content: String): String =
    content.trim match {
      case s if s.startsWith("{") || s.startsWith("[")             => "application/json"
      case s if s.startsWith("<!DOCTYPE") || s.startsWith("<html") => "text/html"
      case s if s.startsWith("<?xml")                              => "application/xml"
      case _                                                       => "text/plain"
    }

  private def errorJson(message: String): String =
    s"""{"error":"${message.replace("\"", "\\\"")}","timestamp":"${java.time.Instant.now()}"}"""
}

/**
 * Default 404 handler
 */
class DefaultHandler(knownPaths: List[String]) extends HttpHandler {
  override def handle(exchange: HttpExchange): Unit = {
    val path        = exchange.getRequestURI.getPath
    val suggestions = knownPaths.filter(_.contains(path.split("/").last)).take(3)

    val response = s"""{
      "error": "Not Found",
      "path": "$path",
      "suggestions": [${suggestions.map(s => s""""$s"""").mkString(",")}]
    }"""

    exchange.getResponseHeaders.add("Content-Type", "application/json")
    val bytes = response.getBytes("UTF-8")
    exchange.sendResponseHeaders(404, bytes.length.toLong)

    Using.resource(exchange.getResponseBody)(output => output.write(bytes))
  }
}
