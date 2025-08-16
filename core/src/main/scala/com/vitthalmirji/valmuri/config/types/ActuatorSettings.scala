package com.vitthalmirji.valmuri.config.types

case class ActuatorSettings(
  enabled: Boolean,
  basePath: String = "/actuator",
  healthPath: String = "/health",
  metricsPath: String = "/metrics",
  infoPath: String = "/info"
)
