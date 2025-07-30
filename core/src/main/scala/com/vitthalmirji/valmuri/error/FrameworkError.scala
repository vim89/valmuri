package com.vitthalmirji.valmuri.error

object FrameworkError {
  final case class ConfigError(msg: String) extends FrameworkError(msg, "CONFIG_ERROR")

  final case class ServiceError(msg: String) extends FrameworkError(msg, "SERVICE_ERROR")

  final case class RoutingError(msg: String) extends FrameworkError(msg, "ROUTING_ERROR")

  final case class MissingParameter(param: String) extends FrameworkError(s"Missing required parameter: $param", "MISSING_PARAM")

  final case class InvalidParameter(param: String, reason: String) extends FrameworkError(s"Invalid parameter $param: $reason", "INVALID_PARAM")

  final case class UnexpectedError(msg: String) extends FrameworkError(msg, "UNEXPECTED_ERROR")
}

/**
 * Framework error hierarchy with variance
 */
abstract class FrameworkError(val message: String, val code: String) extends Product with Serializable
