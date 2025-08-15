package com.vitthalmirji.valmuri.handler

import com.sun.net.httpserver.{ HttpExchange, HttpHandler }

import java.nio.file.{ Files, Paths }

class StaticFileHandler extends HttpHandler {
  def handle(exchange: HttpExchange): Unit = {
    val path     = exchange.getRequestURI.getPath.substring("/static".length)
    val filePath = Paths.get("src/main/resources/static" + path)

    if (Files.exists(filePath)) {
      val content     = Files.readAllBytes(filePath)
      val contentType = getContentType(path)

      exchange.getResponseHeaders.set("Content-Type", contentType)
      exchange.sendResponseHeaders(200, content.length)
      exchange.getResponseBody.write(content)
      exchange.getResponseBody.close()
    } else {
      val notFound = "File not found".getBytes
      exchange.sendResponseHeaders(404, notFound.length)
      exchange.getResponseBody.write(notFound)
      exchange.getResponseBody.close()
    }
  }

  private def getContentType(path: String): String =
    path.toLowerCase match {
      case p if p.endsWith(".html")                       => "text/html"
      case p if p.endsWith(".css")                        => "text/css"
      case p if p.endsWith(".js")                         => "application/javascript"
      case p if p.endsWith(".png")                        => "image/png"
      case p if p.endsWith(".jpg") || p.endsWith(".jpeg") => "image/jpeg"
      case _                                              => "text/plain"
    }
}
