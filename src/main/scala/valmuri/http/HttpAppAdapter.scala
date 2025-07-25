package valmuri.http

import valmuri.routing.{Router, Request => ValmuriRequest, Response => ValmuriResponse}
import zio._
import zio.http._
import zio.prelude.data.Optional.AllValuesAreNullable

object HttpAppAdapter {
  def fromRouter(router: Router): Routes[Any, Response] =
    Routes(
      RoutePattern.any -> handler { (req: Request) =>
        val valmuriRequest = ValmuriRequest(
          method = req.method.toString,
          path = req.url.path.toString,
          params = Map.empty // Path params will be extracted by router
        )

        val valmuriResponse = router.route(valmuriRequest)

        ZIO.succeed(
          Response(
            status = Status.fromInt(valmuriResponse.status).getOrElse(Status.InternalServerError),
            headers = Headers(Header.ContentType(MediaType.application.json)),
            body = Body.fromString(valmuriResponse.body)
          )
        )
      }
    )
}
