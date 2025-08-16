package valmuri.test

import com.vitthalmirji.valmuri.core.VApplicationEnhanced
import com.vitthalmirji.valmuri.http.{ HttpMethod, VRequest, VRoute }
import com.vitthalmirji.valmuri.error.VResult
import munit.FunSuite

class EnhancedFrameworkTest extends FunSuite {

  // Test application using enhanced features
  object TestApp extends VApplicationEnhanced {
    override def configure(): VResult[Unit] =
      for {
        _ <- registerServiceWithLifecycle[String]("test-string-service")
      } yield ()

    override def routes(): List[VRoute] = List(
      VRoute("/test", _ => VResult.success("Enhanced framework working!")),
      VRoute(
        "/config-test",
        _ => {
          val config = getEnhancedConfig
          VResult.success(s"App: ${config.appName}, Port: ${config.server.port}")
        }
      )
    )
  }

  test("enhanced application should start without breaking existing functionality") {
    // This test ensures that enhanced features don't break existing VApplication
    val result = TestApp.configure()
    assert(result.isSuccess)

    val routes = TestApp.routes()
    assertEquals(routes.length, 2)

    // Test that enhanced config works
    val config = TestApp.getEnhancedConfig
    assertNotEquals(config.appName, "")
    assert(config.server.port > 0)
  }

  test("should provide backward compatibility with original VApplication") {
    // Create an instance that uses original VApplication features
    val routes    = TestApp.routes()
    val testRoute = routes.find(_.path == "/test").get

    // Mock request (you'll need to implement VRequest if not already done)
    val mockRequest = createMockRequest("/test")
    val response    = testRoute.handler(VRequest("/test", HttpMethod.GET, mockRequest.pathParams))

    assert(response.isSuccess)
    assertEquals(response.getOrElse("invalid"), "Enhanced framework working!")
  }

  // Helper method for creating mock requests
  case class MockRequest(pathParams: Map[String, String] = Map.empty)

  // Helper method for creating mock requests
  private def createMockRequest(path: String): MockRequest =
    MockRequest()
}
