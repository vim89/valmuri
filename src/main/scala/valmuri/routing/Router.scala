package valmuri.routing

case class Request(method: String, path: String, params: Map[String, String] = Map.empty)
case class Response(status: Int, body: String)

class Router private (private val routes: List[RouteDefinition]) {
  def route(request: Request): Response =
    routes.find { routeDef =>
      routeDef.method == request.method && routeDef.pattern.matches(request.path).isDefined
    } match {
      case Some(routeDef) =>
        val params          = routeDef.pattern.matches(request.path).getOrElse(Map.empty)
        val enrichedRequest = request.copy(params = params)
        routeDef.handler.handle(enrichedRequest)
      case None =>
        Response(404, s"No route for ${request.method} ${request.path}")
    }
}

object Router {
  def apply(routes: RouteDefinition*): Router = new Router(routes.toList)
}
