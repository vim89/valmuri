package valmuri.routing

final case class Route(method: String, path: String)

case class RouteDefinition(method: String, pattern: PathPattern, handler: Handler)
