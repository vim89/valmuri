package valmuri.test

import com.vitthalmirji.valmuri.config.VConfig
import com.vitthalmirji.valmuri.config.types.VConfigExtended
import munit.FunSuite

class VConfigExtendedTest extends FunSuite {

  test("should create enhanced config from existing VConfig") {
    // Create a VConfig using your existing implementation
    val originalConfig = VConfig(
      appName = "Test App",
      appVersion = "1.0.0",
      serverPort = 9090,
      corsOrigin = "http://localhost:3000"
    )

    // Create enhanced config
    val enhancedConfig = VConfigExtended.fromVConfig(originalConfig)

    // Test backward compatibility
    assertEquals(enhancedConfig.appName, "Test App")
    assertEquals(enhancedConfig.serverPort, 9090)
    assertEquals(enhancedConfig.actuatorEnabled, true)

    // Test enhanced features
    assertEquals(enhancedConfig.server.host, "localhost")
    assertEquals(enhancedConfig.server.port, 9090)
    assertEquals(enhancedConfig.actuator.enabled, true)
    assertEquals(enhancedConfig.cors.enabled, true)
    assertEquals(enhancedConfig.cors.origin, "http://localhost:3000")
  }

  test("should validate server settings") {
    val config   = VConfig(serverPort = 80) // Invalid port for non-root
    val enhanced = VConfigExtended.fromVConfig(config)

    assert(!enhanced.server.isValidPort) // Port 80 requires root privileges
  }

  test("should handle missing optional settings") {
    val config   = VConfig(databaseUrl = None)
    val enhanced = VConfigExtended.fromVConfig(config)

    assertEquals(enhanced.database.url, None)
    assertEquals(enhanced.database.maxConnections, 10) // Default value
  }
}
