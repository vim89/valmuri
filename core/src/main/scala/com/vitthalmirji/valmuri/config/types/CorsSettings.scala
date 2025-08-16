package com.vitthalmirji.valmuri.config.types

case class CorsSettings(
  enabled: Boolean,
  origin: String,
  methods: List[String] = List("GET", "POST", "PUT", "DELETE", "OPTIONS"),
  headers: List[String] = List("Content-Type", "Authorization")
)
