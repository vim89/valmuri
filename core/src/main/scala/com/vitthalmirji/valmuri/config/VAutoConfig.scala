package com.vitthalmirji.valmuri.config

import com.vitthalmirji.valmuri.di.VServices
import com.vitthalmirji.valmuri.http.VActuator

/**
 * Autoconfiguration system like Spring Boot
 */
class VAutoConfig(config: VConfig, services: VServices) {

  def configure(): Unit = {
    println("🔧 Auto-configuring Valmuri components...")

    // Always configure core components
    services.register[VConfig](config)

    // Configure actuator if enabled
    if (config.actuatorEnabled) {
      val actuator = new VActuator()
      services.register[VActuator](actuator)
      println("📊 Actuator configured")
    }

    println("✅ Auto-configuration complete")
  }
}
