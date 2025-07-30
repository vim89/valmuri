package com.vitthalmirji.valmuri

object HttpMethod {
  case object GET extends HttpMethod

  case object POST extends HttpMethod

  case object PUT extends HttpMethod

  case object DELETE extends HttpMethod

  case object PATCH extends HttpMethod

  case object OPTIONS extends HttpMethod

  // Pattern matching factory
  def fromString(method: String): HttpMethod = method.toUpperCase match {
    case "GET" => GET
    case "POST" => POST
    case "PUT" => PUT
    case "DELETE" => DELETE
    case "PATCH" => PATCH
    case "OPTIONS" => OPTIONS
    case _ => GET // Default fallback
  }
}

/**
 * HTTP methods as ADT (Algebraic Data Type)
 */
sealed trait HttpMethod extends Product with Serializable