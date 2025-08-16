package com.vitthalmirji.valmuri.config.types

/**
 * Type-safe configuration sections for better organization
 * These work alongside your existing VConfig without breaking changes
 */

case class ServerSettings(
  host: String,
  port: Int,
  threads: Int,
  backlog: Int,
  shutdownDelay: Int,
  maxRequestSize: Int
) {
  // Helper methods for validation
  def isValidPort: Boolean = port >= 1024 && port <= 65535
  def isValidHost: Boolean = host.nonEmpty
}
