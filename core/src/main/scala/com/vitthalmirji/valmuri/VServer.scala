package com.vitthalmirji.valmuri

import com.sun.net.httpserver.{HttpExchange, HttpHandler, HttpServer}
import com.vitthalmirji.valmuri.error.FrameworkError

import java.net.InetSocketAddress
import scala.util.Try
import scala.jdk.CollectionConverters._

/**
 * Enhanced HTTP server - Fixed for compilation
 */
private[valmuri] class VServer(host: String, port: Int, routes: List[VRoute]) {

  private var serverInstance: Option[HttpServer] = None

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

  private def registerRoute(server: HttpServer, route: VRoute): Unit = {
    server.createContext(route.path, new EnhancedVHandler(route)): Unit
  }
}

/**
 * Enhanced HTTP handler - Fixed imports and error handling
 */
private class EnhancedVHandler(route: VRoute) extends HttpHandler {

  def handle(exchange: HttpExchange): Unit = {
    val request = extractRequest(exchange)

    val responseResult = route.handler(request)

    responseResult match {
      case VResult.Success(content) =>
        sendSuccessResponse(exchange, content)
      case VResult.Failure(error) =>
        sendErrorResponse(exchange, error)
    }
  }

  private def extractRequest(exchange: HttpExchange): VRequest = {
    val uri = exchange.getRequestURI
    val method = HttpMethod.fromString(exchange.getRequestMethod)

    val headers = exchange.getRequestHeaders.asScala.view
      .mapValues(_.asScala.headOption.getOrElse("")).toMap

    val body = extractBody(exchange, method)
    val params = extractParams(uri)

    VRequest(
      path = uri.getPath,
      method = method,
      params = params,
      headers = headers,
      body = body
    )
  }

  private def extractBody(exchange: HttpExchange, method: HttpMethod): Option[String] = {
    method match {
      case HttpMethod.POST | HttpMethod.PUT | HttpMethod.PATCH =>
        Try {
          val inputStream = exchange.getRequestBody
          val body = scala.io.Source.fromInputStream(inputStream, "UTF-8").mkString
          inputStream.close()
          if (body.nonEmpty) Some(body) else None
        }.getOrElse(None)
      case _ => None
    }
  }

  private def extractParams(uri: java.net.URI): Map[String, String] = {
    Option(uri.getQuery).fold(Map.empty[String, String]) { query =>
      query.split("&").flatMap { param =>
        param.split("=", 2) match {
          case Array(key, value) => Some(key -> java.net.URLDecoder.decode(value, "UTF-8"))
          case Array(key) => Some(key -> "")
          case _ => None
        }
      }.toMap
    }
  }

  private def sendSuccessResponse(exchange: HttpExchange, content: String): Unit = {
    sendResponse(exchange, 200, content, getContentType(content))
  }

  private def sendErrorResponse(exchange: HttpExchange, error: FrameworkError): Unit = {
    val (statusCode, content) = error match {
      case FrameworkError.MissingParameter(_) | FrameworkError.InvalidParameter(_, _) =>
        (400, createErrorJson(error))
      case FrameworkError.RoutingError(_) =>
        (404, createErrorJson(error))
      case FrameworkError.ConfigError(_) | FrameworkError.ServiceError(_) =>
        (500, createErrorJson(error))
      case FrameworkError.UnexpectedError(_) =>
        (500, createErrorJson(error))
    }

    sendResponse(exchange, statusCode, content, "application/json")
  }

  private def sendResponse(exchange: HttpExchange, statusCode: Int, content: String, contentType: String): Unit = {
    Try {
      val responseBytes = content.getBytes("UTF-8")

      exchange.getResponseHeaders.set("Content-Type", contentType)
      exchange.getResponseHeaders.set("Access-Control-Allow-Origin", "*")
      exchange.getResponseHeaders.set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
      exchange.getResponseHeaders.set("Access-Control-Allow-Headers", "Content-Type, Authorization")

      exchange.sendResponseHeaders(statusCode, responseBytes.length.toLong)
      val outputStream = exchange.getResponseBody
      outputStream.write(responseBytes)
      outputStream.close()
    }.recover { case ex =>
      println(s"❌ Error sending response: ${ex.getMessage}")
    }: Unit
  }

  private def getContentType(content: String): String = {
    content.trim match {
      case json if json.startsWith("{") || json.startsWith("[") => "application/json"
      case html if html.startsWith("<") => "text/html"
      case _ => "text/plain"
    }
  }

  private def createErrorJson(error: FrameworkError): String = {
    s"""{
      "error": "${error.code}",
      "message": "${error.message}",
      "timestamp": "${java.time.Instant.now()}"
    }"""
  }
}

/**
 * Default handler for unmatched routes
 */
private class DefaultHandler(routes: List[VRoute]) extends HttpHandler {

  def handle(exchange: HttpExchange): Unit = {
    val path = exchange.getRequestURI.getPath
    val method = exchange.getRequestMethod

    val hasMatchingRoute = routes.exists(_.path == path)

    val (statusCode, content) = if (hasMatchingRoute) {
      (405, s"""{"error": "METHOD_NOT_ALLOWED", "message": "Method $method not allowed for $path"}""")
    } else {
      (404, s"""{"error": "NOT_FOUND", "message": "No route found for $method $path"}""")
    }

    val responseBytes = content.getBytes("UTF-8")
    exchange.getResponseHeaders.set("Content-Type", "application/json")
    exchange.sendResponseHeaders(statusCode, responseBytes.length.toLong)
    val outputStream = exchange.getResponseBody
    outputStream.write(responseBytes)
    outputStream.close()
  }
}
