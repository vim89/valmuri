package valmuri.routing

object DSL {
  // DSL for building path patterns
  case class MethodBuilder(method: String) {
    def /(segment: String): PathBuilder =
      PathBuilder(method, List(Literal(segment)))

    def /(param: PathSegment): PathBuilder =
      PathBuilder(method, List(param))

    def root: PathBuilder = PathBuilder(method, List(Root))
  }

  case class PathBuilder(method: String, segments: List[PathSegment]) {
    def /(segment: String): PathBuilder =
      copy(segments = segments :+ Literal(segment))

    def /(param: PathSegment): PathBuilder =
      copy(segments = segments :+ param)

    def ->(handler: Handler): RouteDefinition =
      RouteDefinition(method, PathPattern(segments), handler)
  }

  val GET = MethodBuilder("GET")
  val POST = MethodBuilder("POST")
  val PUT = MethodBuilder("PUT")
  val DELETE = MethodBuilder("DELETE")

  // Parameter extractors
  def StringParam(name: String): StringParam = new StringParam(name)
  def IntParam(name: String): IntParam = new IntParam(name)
}
