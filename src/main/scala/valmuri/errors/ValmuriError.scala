package valmuri.errors

import zio.http._

sealed trait ValmuriError extends Throwable {
  def message: String
  def cause: Option[Throwable] = None
  def toResponse: Response
}

object ValmuriError {
  case class ValidationError(
    field: String,
    message: String,
    override val cause: Option[Throwable] = None)
      extends ValmuriError {
    def toResponse: Response = Response
      .json(s"""{"error": "validation", "field": "$field", "message": "$message"}""")
      .status(Status.BadRequest)
  }

  case class NotFoundError(resource: String) extends ValmuriError {
    def message: String = s"$resource not found"
    def toResponse: Response =
      Response.json(s"""{"error": "not_found", "resource": "$resource"}""").status(Status.NotFound)
  }

  case class DatabaseError(
    override val message: String,
    override val cause: Option[Throwable] = None)
      extends ValmuriError {
    def toResponse: Response =
      Response.json(s"""{"error": "database", "message": "$message"}""").status(Status.InternalServerError)
  }

  case class ConfigurationError(
    override val message: String,
    override val cause: Option[Throwable] = None)
      extends ValmuriError {
    def toResponse: Response =
      Response.json(s"""{"error": "configuration", "message": "$message"}""").status(Status.InternalServerError)
  }
}
