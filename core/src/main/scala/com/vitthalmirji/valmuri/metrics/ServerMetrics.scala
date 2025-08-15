package com.vitthalmirji.valmuri.metrics

/**
 * Server metrics collection
 */

object ServerMetrics {
  @volatile private var totalRequests: Long    = 0
  @volatile private var activeConnections: Int = 0
  private val startTime                        = System.currentTimeMillis()

  def incrementRequests(): Unit = totalRequests += 1

  def incrementConnections(): Unit = activeConnections += 1

  def decrementConnections(): Unit = activeConnections -= 1

  def getHealth: ServerHealth = ServerHealth(
    status = "UP",
    uptime = System.currentTimeMillis() - startTime,
    activeConnections = activeConnections,
    totalRequests = totalRequests
  )
}
