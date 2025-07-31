package com.vitthalmirji.valmuri

import com.vitthalmirji.valmuri.HttpMethod.{
  GET, POST, PUT, DELETE
}
import com.vitthalmirji.valmuri.HttpMethod
import com.vitthalmirji.valmuri.error.FrameworkError

import scala.util.Try

/**
 * Enhanced request with pattern matching support
 */
case class VRequest(path: String, method: HttpMethod, params: Map[String, String] = Map.empty,
                    headers: Map[String, String] = Map.empty, body: Option[String] = None) {
  // Pattern matching helpers
  def isGet: Boolean = method match {
    case GET => true
    case _   => false
  }

  def isPost: Boolean = method match {
    case POST => true
    case _    => false
  }

  def isPut: Boolean = method match {
    case PUT => true
    case _   => false
  }

  def isDelete: Boolean = method match {
    case DELETE => true
    case _      => false
  }

  // Safe parameter extraction with partial functions
  def getParam(key: String): Option[String] = params.get(key)

  def getRequiredParam(key: String): VResult[String] =
    params.get(key) match {
      case Some(value) => VResult.success(value)
      case None => VResult.failure(FrameworkError.MissingParameter(key))
    }

  // Type-safe parameter extraction
  def getIntParam(key: String): VResult[Int] =
    getRequiredParam(key).flatMap { value =>
      VResult.fromTry(Try(value.toInt))
        .mapError(_ => FrameworkError.InvalidParameter(key, "Expected integer"))
    }
}
