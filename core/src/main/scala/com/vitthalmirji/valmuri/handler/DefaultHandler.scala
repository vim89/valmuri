package com.vitthalmirji.valmuri.handler

import com.sun.net.httpserver.{ HttpExchange, HttpHandler }
import com.vitthalmirji.valmuri.http.VRoute

/**
 * Default handler for unmatched routes
 */
class DefaultHandler(routes: List[VRoute]) extends HttpHandler {

  def handle(exchange: HttpExchange): Unit = {
    val path   = exchange.getRequestURI.getPath
    val method = exchange.getRequestMethod

    // Find routes whose path is a prefix of the request path
    val matchingRoutes = routes.filter(r => path.startsWith(r.path))

    val (statusCode, content) = if (matchingRoutes.isEmpty) {
      // No matching prefix -> 404
      (404, s"""{"error": "NOT_FOUND", "message": "No route found for $method $path"}""")
    } else {
      // Prefix match found but exact method match missing -> 405
      (405, s"""{"error": "METHOD_NOT_ALLOWED", "message": "Method $method not allowed for $path"}""")
    }

    val responseBytes = content.getBytes("UTF-8")
    exchange.getResponseHeaders.set("Content-Type", "application/json")
    exchange.sendResponseHeaders(statusCode, responseBytes.length.toLong)
    val outputStream = exchange.getResponseBody
    outputStream.write(responseBytes)
    outputStream.close()
  }
}
