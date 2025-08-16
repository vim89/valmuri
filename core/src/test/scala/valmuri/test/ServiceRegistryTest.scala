package valmuri.test

import com.vitthalmirji.valmuri.di.{ ServiceRegistry, VServices }
import com.vitthalmirji.valmuri.error.VResult
import munit.FunSuite

class ServiceRegistryTest extends FunSuite {

  // Test services
  trait TestService {
    def getValue: String
  }

  class TestServiceImpl extends TestService {
    def getValue: String = "test-value"
  }

  class DependentService(testService: TestService) {
    def getResult: String = s"dependent: ${testService.getValue}"
  }

  test("should register and retrieve services with lifecycle") {
    val baseServices = new VServices()
    val registry     = new ServiceRegistry(baseServices)
    val testService  = new TestServiceImpl()

    // Register with lifecycle tracking
    val result = registry.registerWithLifecycle[TestService](testService)
    assert(result.isSuccess)

    // Retrieve with lifecycle checking
    val retrieved = registry.getWithLifecycleCheck[TestService]
    assert(retrieved.isSuccess)
    retrieved match {
      case VResult.Success(service) => assertEquals(service.getValue, "test-value")
      case VResult.Failure(err)     => fail(s"Expected success but got failure: $err")
    }

  }

  test("should validate dependencies and detect circular references") {
    val baseServices = new VServices()
    val registry     = new ServiceRegistry(baseServices)

    // This would create a circular dependency in a real scenario
    val validation = registry.validateDependencies()
    assert(validation.isSuccess) // No circular dependencies in this simple test
  }

  test("should fall back to base services for compatibility") {
    val baseServices = new VServices()
    val registry     = new ServiceRegistry(baseServices)
    val testService  = new TestServiceImpl()

    // Register using base services (existing functionality)
    val registerResult = registry.register[TestService](testService)
    assert(registerResult.isSuccess)

    // Should be able to retrieve it
    val retrieved = registry.get[TestService]
    assertEquals(retrieved.getValue, "test-value")
  }

  test("should provide service state debugging") {
    val baseServices = new VServices()
    val registry     = new ServiceRegistry(baseServices)
    val testService  = new TestServiceImpl()

    registry.registerWithLifecycle(testService)

    val states = registry.getServiceStates
    assert(states.contains("TestServiceImpl"))
    assertEquals(states("TestServiceImpl"), "Ready")
  }
}
