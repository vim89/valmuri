package com.vitthalmirji.valmuri.di

import com.vitthalmirji.valmuri.encoder.{ JsonEncoder, ResponseEncoder }
import com.vitthalmirji.valmuri.error.{ VResult, ValmuriError }
import com.vitthalmirji.valmuri.http.{ HttpMethod, VRequest, VRoute }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Enhanced controller base class with type classes and functional programming
 */
abstract class VController {

  // Type aliases for cleaner signatures
  type ControllerAction[A] = VRequest => VResult[A]
  type AsyncAction[A]      = VRequest => Future[VResult[A]]

  def routes(): List[VRoute]

  // Enhanced helper methods with type classes
  protected def ok[A: ResponseEncoder](content: A): VResult[String] =
    VResult.success(implicitly[ResponseEncoder[A]].encode(content))

  protected def json[A: JsonEncoder](content: A): VResult[String] =
    VResult.success(implicitly[JsonEncoder[A]].toJson(content))

  protected def created[A: ResponseEncoder](content: A): VResult[String] =
    VResult.success(implicitly[ResponseEncoder[A]].encode(content))

  protected def notFound(message: String = "Not Found"): VResult[String] =
    VResult.failure(ValmuriError.RoutingError(message))

  protected def badRequest(message: String): VResult[String] =
    VResult.failure(ValmuriError.InvalidParameter("request", message))

  protected def error(message: String): VResult[String] =
    VResult.failure(ValmuriError.UnexpectedError(message))

  // Async action support
  protected def async[A: ResponseEncoder](action: AsyncAction[A]): ControllerAction[String] = { req =>
    VResult.fromFuture(
      action(req).map {
        case VResult.Success(a)   => implicitly[ResponseEncoder[A]].encode(a)
        case VResult.Failure(err) => throw new RuntimeException(err.message)
      }
    )
  }

  // Pattern matching for request method routing
  protected def matchMethod[A](request: VRequest)(
    get: => VResult[A] = VResult.failure(ValmuriError.RoutingError("GET not supported")),
    post: => VResult[A] = VResult.failure(ValmuriError.RoutingError("POST not supported")),
    put: => VResult[A] = VResult.failure(ValmuriError.RoutingError("PUT not supported")),
    delete: => VResult[A] = VResult.failure(ValmuriError.RoutingError("DELETE not supported"))
  ): VResult[A] =
    request.method match {
      case HttpMethod.GET    => get
      case HttpMethod.POST   => post
      case HttpMethod.PUT    => put
      case HttpMethod.DELETE => delete
      case _                 => VResult.failure(ValmuriError.RoutingError(s"Method ${request.method} not supported"))
    }
}
