package com.vitthalmirji.valmuri.config.types

case class DatabaseSettings(
  url: Option[String],
  driver: Option[String],
  username: Option[String],
  password: Option[String],
  maxConnections: Int = 10,
  connectionTimeout: Int = 30000
)
