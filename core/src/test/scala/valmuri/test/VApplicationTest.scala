package valmuri.test

import com.vitthalmirji.valmuri.config.VConfig

class VApplicationTest extends munit.FunSuite {
  test("VConfig should load with defaults") {
    val config = VConfig()
    assertEquals(config.serverPort, 8080)
    assertEquals(config.serverHost, "localhost")
  }
}
