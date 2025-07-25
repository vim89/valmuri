package valmuri.hello

import valmuri.routing._

object AppRoutes {
  val router: Router = Router {
    case GET("/") =>
      Handler(_ => Response(200, "🎉 Welcome to Valmuri!"))

    case GET("/health") =>
      Handler(_ => Response(200, "✅ OK"))
  }
}
