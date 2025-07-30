package com.vitthalmirji.valmuri.error

// Custom exceptions for pattern matching
case class ConfigurationError(message: String) extends Exception(message)
