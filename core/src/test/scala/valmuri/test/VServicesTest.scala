package valmuri.test

import com.vitthalmirji.valmuri.di.VServices

class VServicesTest extends munit.FunSuite {

  trait TestService {
    def getMessage: String
  }

  class TestServiceImpl extends TestService {
    def getMessage = "Hello from service!"
  }

  test("VServices should register and retrieve services") {
    val services    = new VServices()
    val testService = new TestServiceImpl()

    services.register[TestService](testService)
    val retrieved = services.get[TestService]

    assertEquals(retrieved.getMessage, "Hello from service!")
  }
}
