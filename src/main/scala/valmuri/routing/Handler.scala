package valmuri.routing

trait Handler {
  def handle(request: Request): Response
}

object Handler {
  def apply(f: Request => Response): Handler = new Handler {
    def handle(request: Request): Response = f(request)
  }
}
