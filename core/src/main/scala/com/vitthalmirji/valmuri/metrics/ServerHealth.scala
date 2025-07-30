package com.vitthalmirji.valmuri.metrics

/**
 * Server health monitoring
 */
case class ServerHealth(status: String, uptime: Long, activeConnections: Int, totalRequests: Long)
