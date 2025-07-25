package valmuri.hello

import valmuri.routing.DSL._
import valmuri.routing.{Handler, Response, Router}

object AppRoutes {
  val router: Router = Router(
    // Root path
    GET.root -> Handler(_ => Response(200, "🎉 Welcome to Valmuri with DI!")),

    // Health check with DI service
    GET / "health" -> Handler { _ =>
      // This would use injected HealthService
      Response(200, """{"status": "OK", "framework": "Valmuri", "di": "enabled"}""")
    },

    // Config endpoint
    GET / "config" -> Handler { _ =>
      // This would use injected ConfigLoader
      Response(200, """{"environment": "development", "di_enabled": true}""")
    },

    // Users API with path parameters
    GET / "users" / IntParam("id") -> Handler { request =>
      val userId = request.params("id")
      Response(200, s"""{"id": $userId, "name": "User $userId", "loaded_via": "DI"}""")
    },

    // DI status endpoint
    GET / "di" / "status" -> Handler(_ =>
      Response(200, """{"container": "active", "services": ["HealthService", "ConfigLoader"]}""")
    )
  )
}
