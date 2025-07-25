package valmuri.test.routing

import munit.FunSuite
import valmuri.routing.DSL._
import valmuri.routing.{Handler, PathPattern, Request, Response, Router}
import valmuri.routing.Literal

class RouterTest extends FunSuite {

  test("Router should handle root path") {
    val router = Router(
      GET.root -> Handler(_ => Response(200, "Hello World"))
    )

    val response = router.route(Request("GET", "/"))
    assertEquals(response.status, 200)
    assertEquals(response.body, "Hello World")
  }

  test("Router should handle literal paths") {
    val router = Router(
      GET / "health" -> Handler(_ => Response(200, "OK"))
    )

    val response = router.route(Request("GET", "/health"))
    assertEquals(response.status, 200)
    assertEquals(response.body, "OK")
  }

  test("Router should handle path parameters") {
    val router = Router(
      GET / "users" / IntParam("id") -> Handler { request =>
        val userId = request.params("id")
        Response(200, s"User $userId")
      }
    )

    val response = router.route(Request("GET", "/users/123"))
    assertEquals(response.status, 200)
    assertEquals(response.body, "User 123")
  }

  test("Router should handle multiple path parameters") {
    val router = Router(
      GET / "users" / IntParam("userId") / "posts" / IntParam("postId") ->
        Handler { request =>
          val userId = request.params("userId")
          val postId = request.params("postId")
          Response(200, s"User $userId, Post $postId")
        }
    )

    val response = router.route(Request("GET", "/users/123/posts/456"))
    assertEquals(response.status, 200)
    assertEquals(response.body, "User 123, Post 456")
  }

  test("Router should return 404 for unknown routes") {
    val router = Router(
      GET / "health" -> Handler(_ => Response(200, "OK"))
    )

    val response = router.route(Request("GET", "/unknown"))
    assertEquals(response.status, 404)
    assert(response.body.contains("No route for GET /unknown"))
  }

  test("Router should handle different HTTP methods") {
    val router = Router(
      GET / "users" -> Handler(_ => Response(200, "GET users")),
      POST / "users" -> Handler(_ => Response(201, "POST users")),
      PUT / "users" / IntParam("id") -> Handler { request =>
        val userId = request.params("id")
        Response(200, s"PUT user $userId")
      }
    )

    assertEquals(router.route(Request("GET", "/users")).body, "GET users")
    assertEquals(router.route(Request("POST", "/users")).body, "POST users")
    assertEquals(router.route(Request("PUT", "/users/123")).body, "PUT user 123")
  }

  test("PathPattern should match complex nested routes") {
    val pattern = PathPattern(List(
      Literal("api"),
      Literal("v1"),
      Literal("users"),
      IntParam("id"),
      Literal("posts")
    ))

    val result = pattern.matches("/api/v1/users/123/posts")
    assert(result.isDefined)
    assertEquals(result.get, Map("id" -> "123"))
  }

  test("IntParam should reject non-numeric values") {
    val router = Router(
      GET / "users" / IntParam("id") -> Handler { request =>
        Response(200, "Found user")
      }
    )

    val response = router.route(Request("GET", "/users/abc"))
    assertEquals(response.status, 404)
  }
}
