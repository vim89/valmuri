package valmuri.hello

import valmuri.routing.DSL._
import valmuri.routing.{Handler, Response, Router}

object AppRoutes {
  val router: Router = Router(
    // Root path
    GET.root -> Handler(_ => Response(200, "🎉 Welcome to Valmuri!")),

    // Health check
    GET / "health" -> Handler(_ => Response(200, "✅ OK")),

    // Users API with path parameters
    GET / "users" / IntParam("id") -> Handler { request =>
      val userId = request.params("id")
      Response(200, s"""{"id": $userId, "name": "User $userId"}""")
    },

    // List all users
    GET / "users" -> Handler(_ =>
      Response(200, """[{"id": 1, "name": "John"}, {"id": 2, "name": "Jane"}]""")
    ),

    // Create user
    POST / "users" -> Handler { request =>
      Response(201, """{"id": 3, "name": "New User", "status": "created"}""")
    },

    // Nested routes
    GET / "api" / "v1" / "status" -> Handler(_ =>
      Response(200, """{"version": "1.0", "status": "running"}""")
    )
  )
}
