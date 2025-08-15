package com.vitthalmirji.valmuri.handler

import com.sun.net.httpserver.{ HttpExchange, HttpHandler }
import com.vitthalmirji.valmuri.VRoute

/**
 * Default handler for unmatched routes
 */
class DefaultHandler(routes: List[VRoute]) extends HttpHandler {

  def handle(exchange: HttpExchange): Unit = {
    val path   = exchange.getRequestURI.getPath
    val method = exchange.getRequestMethod

    // Check if any route pattern might match (simplified)
    val hasMatchingRoute = routes.exists(_.path == path)

    val (statusCode, content) = if (hasMatchingRoute) {
      // Route exists but method not allowed
      (405, s"""{"error": "METHOD_NOT_ALLOWED", "message": "Method $method not allowed for $path"}""")
    } else {
      // Route not found
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
