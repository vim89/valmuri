package valmuri.http

import valmuri.routing.{ Request => ValmuriRequest, Response => ValmuriResponse, Router }
import zio._
import zio.http._
import zio.prelude.data.Optional.AllValuesAreNullable

object HttpAppAdapter {
  def fromRouter(router: Router): Routes[Any, Response] =
    Routes(
      Method.ANY / trailing -> handler { (req: Request) =>
        val valmuriRequest = ValmuriRequest(
          method = req.method.toString,
          path = req.url.path.toString,
          params = Map.empty, // Path params will be extracted by router
        )

        val valmuriResponse = router.route(valmuriRequest)

        val status = Status.fromInt(valmuriResponse.status).getOrElse(Status.InternalServerError)

        ZIO.succeed(
          Response(
            status = status,
            headers = Headers(Header.ContentType(MediaType.application.json)),
            body = Body.fromString(valmuriResponse.body),
          )
        )
      }
    )
}
