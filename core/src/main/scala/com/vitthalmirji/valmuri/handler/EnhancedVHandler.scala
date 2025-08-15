package com.vitthalmirji.valmuri.handler

import com.sun.net.httpserver.{ HttpExchange, HttpHandler }
import com.vitthalmirji.valmuri._
import com.vitthalmirji.valmuri.error.{ VResult, ValmuriError }
import com.vitthalmirji.valmuri.http.{ HttpMethod, VRequest, VRoute }

import scala.util.Try

/**
 * Enhanced HTTP handler with functional error handling and pattern matching
 */
class EnhancedVHandler(route: VRoute) extends HttpHandler {

  def handle(exchange: HttpExchange): Unit = {
    // Extract request information
    val request = extractRequest(exchange)

    // Process request with pattern matching on result
    val responseResult = route.handler(request)

    // Send response based on result type
    responseResult match {
      case VResult.Success(content) =>
        sendSuccessResponse(exchange, content)
      case VResult.Failure(error) =>
        sendErrorResponse(exchange, error)
    }
  }

  private def extractRequest(exchange: HttpExchange): VRequest = {
    val uri    = exchange.getRequestURI
    val method = HttpMethod.fromString(exchange.getRequestMethod)

    // Extract headers using pattern matching and partial functions
    val headers = extractHeaders(exchange)

    // Extract body for POST/PUT requests
    val body = extractBody(exchange, method)

    // Extract path parameters (simplified - real implementation would parse :id patterns)
    val params = extractParams(uri)

    VRequest(
      path = uri.getPath,
      method = method,
      params = params,
      headers = headers,
      body = body
    )
  }

  private def extractHeaders(exchange: HttpExchange): Map[String, String] = {
    import scala.jdk.CollectionConverters._

    exchange.getRequestHeaders.asScala.view.mapValues(_.asScala.headOption.getOrElse("")).toMap
  }

  private def extractBody(exchange: HttpExchange, method: HttpMethod): Option[String] =
    method match {
      case m if m == HttpMethod.POST || m == HttpMethod.PUT || m == HttpMethod.PATCH =>
        Try {
          val inputStream = exchange.getRequestBody
          val body        = scala.io.Source.fromInputStream(inputStream).mkString
          inputStream.close()
          if (body.nonEmpty) Some(body) else None
        }.getOrElse(None)
      case _ => None
    }

  private def extractParams(uri: java.net.URI): Map[String, String] =
    Option(uri.getQuery).fold(Map.empty[String, String]) { query =>
      query
        .split("&")
        .flatMap { param =>
          param.split("=", 2) match {
            case Array(key, value) => Some(key -> java.net.URLDecoder.decode(value, "UTF-8"))
            case Array(key)        => Some(key -> "")
            case _                 => None
          }
        }
        .toMap
    }

  private def sendSuccessResponse(exchange: HttpExchange, content: String): Unit =
    sendResponse(exchange, 200, content, getContentType(content))

  private def sendErrorResponse(exchange: HttpExchange, error: ValmuriError): Unit = {
    val (statusCode, content) = error match {
      case ValmuriError.MissingParameter(_) | ValmuriError.InvalidParameter(_, _) =>
        (400, createErrorJson(error))
      case ValmuriError.RoutingError(_) =>
        (404, createErrorJson(error))
      case ValmuriError.ConfigError(_) | ValmuriError.ServiceError(_) =>
        (500, createErrorJson(error))
      case ValmuriError.UnexpectedError(_) =>
        (500, createErrorJson(error))
      case other =>
        (500, createErrorJson(other))
    }

    sendResponse(exchange, statusCode, content, "application/json")
  }

  private def sendResponse(exchange: HttpExchange, statusCode: Int, content: String, contentType: String): Unit = {
    Try {
      val responseBytes = content.getBytes("UTF-8")

      // Set content type header
      exchange.getResponseHeaders.set("Content-Type", contentType)

      // Add CORS headers (basic implementation)
      exchange.getResponseHeaders.set("Access-Control-Allow-Origin", "*")
      exchange.getResponseHeaders.set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
      exchange.getResponseHeaders.set("Access-Control-Allow-Headers", "Content-Type, Authorization")

      exchange.sendResponseHeaders(statusCode, responseBytes.length.toLong)
      val outputStream = exchange.getResponseBody
      outputStream.write(responseBytes)
      outputStream.close()
    }.recover { case ex =>
      // Fallback error handling
      println(s"❌ Error sending response: ${ex.getMessage}")
      ()
    }
    val _ = () // Explicitly discard the Try result to avoid -Wvalue-discard warning
  }

  private def getContentType(content: String): String =
    content.trim match {
      case json if json.startsWith("{") || json.startsWith("[") => "application/json"
      case html if html.startsWith("<")                         => "text/html"
      case _                                                    => "text/plain"
    }

  private def createErrorJson(error: ValmuriError): String =
    s"""{
      "error": "${error.code}",
      "message": "${error.message}",
      "timestamp": "${java.time.Instant.now()}"
    }"""
}
