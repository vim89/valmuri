package valmuri.routing

final case class Request(method: String, path: String)
final case class Response(status: Int, body: String)

class Router private (private val routes: Map[Route, Handler]) {
  def route(request: Request): Response =
    routes.get(Route(request.method, request.path)) match {
      case Some(handler) => handler.handle(request)
      case None          => Response(404, s"No route for ${request.method} ${request.path}")
    }
}

object Router {
  def apply(routeMap: PartialFunction[Route, Handler]): Router = {
    val expanded = routeMap.lift
    val methods = List("GET", "POST", "PUT", "DELETE")
    val paths = List("/", "/hello", "/health")

    val all = for {
      method <- methods
      path   <- paths
      route = Route(method, path)
      handler <- expanded(route)
    } yield route -> handler

    new Router(all.toMap)
  }
}
