package com.vitthalmirji.valmuri.error

/**
 * Framework error hierarchy - Fixed for cross-compilation
 */
sealed abstract class ValmuriError(val message: String, val code: String) extends Product with Serializable

object ValmuriError {
  final case class ConfigError(msg: String) extends ValmuriError(msg, "CONFIG_ERROR")

  final case class ServiceError(msg: String) extends ValmuriError(msg, "SERVICE_ERROR")

  final case class RoutingError(msg: String) extends ValmuriError(msg, "ROUTING_ERROR")

  final case class MissingParameter(param: String)
      extends ValmuriError(s"Missing required parameter: $param", "MISSING_PARAM")

  final case class InvalidParameter(param: String, reason: String)
      extends ValmuriError(s"Invalid parameter $param: $reason", "INVALID_PARAM")

  final case class UnexpectedError(msg: String) extends ValmuriError(msg, "UNEXPECTED_ERROR")

  final case class NotFound(msg: String)     extends ValmuriError(msg, "NOT FOUND")
  final case class BadRequest(msg: String)   extends ValmuriError(msg, "BAD REQUEST")
  final case class Unauthorized(msg: String) extends ValmuriError(msg, "UNAUTHORIZED")
  final case class Forbidden(msg: String)    extends ValmuriError(msg, "FORBIDDEN")
}
