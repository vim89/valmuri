package valmuri.test

import com.vitthalmirji.valmuri.config.VConfig
import com.vitthalmirji.valmuri.{ VRequest, VRoute }

class VApplicationTest extends munit.FunSuite {

  test("VRoute should handle simple request") {
    val route    = VRoute("/hello", _ => "Hello World!")
    val request  = VRequest("/hello", "GET")
    val response = route.handler(request)

    assertEquals(response, "Hello World!")
  }

  test("VConfig should load with defaults") {
    val config = VConfig()
    assertEquals(config.serverPort, 8080)
    assertEquals(config.serverHost, "localhost")
  }
}
