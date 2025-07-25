package valmuri.http

import valmuri.routing.{Response, Router, Request => ValReq}
import zio._
import zio.http.endpoint.openapi.OpenAPI.SecurityScheme.Http
import zio.http.{Request, Status}
import zio.prelude.data.Optional.AllValuesAreNullable

object HttpAppAdapter {
  def fromRouter(router: Router): Http[Any, Throwable, Request, Response] =
    Http.collectZIO[Request] { req =>
      val vmReq = ValReq(req.method.toString(), req.url.path.encode)
      val vmResp = router.route(vmReq)
      ZIO.succeed(
        Response
          .text(vmResp.body)
          .withStatus(Status.fromInt(vmResp.status).getOrElse(Status.InternalServerError))
      )
    }
}
