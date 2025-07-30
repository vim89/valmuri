package com.vitthalmirji.valmuri

import scala.concurrent.Future

object VRoute {
  // Factory methods for different handler types
  def simple(path: String, handler: VRequest => String): VRoute =
    VRoute(path, req => VResult.success(handler(req)))

  def safe(path: String, handler: VRequest => VResult[String]): VRoute =
    VRoute(path, handler)

  def async(path: String, handler: VRequest => Future[String]): VRoute =
    VRoute(path, req => VResult.fromFuture(handler(req)))
}

/**
 * Enhanced route definition with functional error handling
 */
case class VRoute(path: String, handler: VRequest => VResult[String])
