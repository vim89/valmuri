package valmuri.routing

object dsl {
  def GET(path: String): Route = Route("GET", path)
  def POST(path: String): Route = Route("POST", path)
  def PUT(path: String): Route = Route("PUT", path)
  def DELETE(path: String): Route = Route("DELETE", path)
}
