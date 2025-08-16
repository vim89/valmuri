package com.vitthalmirji.valmuri.monitor

import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import scala.jdk.CollectionConverters._

/**
 * Performance monitoring that integrates with your existing framework
 * Tracks request performance and provides metrics for the actuator
 */
class PerformanceMonitor {

  private val requestTimes = new ConcurrentHashMap[String, List[Long]]()
  private val errorCounts  = new ConcurrentHashMap[String, Long]()

  /**
   * Record request timing (call this from your VServer request handling)
   */
  def recordRequest(path: String, durationMs: Long, success: Boolean = true): Unit = {
    // Record timing
    val currentTimes = requestTimes.getOrDefault(path, List.empty)
    val updatedTimes = (durationMs :: currentTimes).take(100) // Keep last 100 requests
    requestTimes.put(path, updatedTimes)

    // Record errors
    if (!success) {
      errorCounts.put(path, errorCounts.getOrDefault(path, 0L) + 1)
    }
  }

  /**
   * Get performance metrics for actuator endpoint
   */
  def getMetrics: String = {
    val pathMetrics = requestTimes.asScala
      .map { case (path, times) =>
        val avgTime      = if (times.nonEmpty) times.sum / times.length else 0
        val maxTime      = if (times.nonEmpty) times.max else 0
        val minTime      = if (times.nonEmpty) times.min else 0
        val requestCount = times.length
        val errorCount   = errorCounts.getOrDefault(path, 0L)

        s""""$path": {
        "requestCount": $requestCount,
        "errorCount": $errorCount,
        "avgResponseTime": $avgTime,
        "maxResponseTime": $maxTime,
        "minResponseTime": $minTime,
        "successRate": ${if (requestCount > 0) ((requestCount - errorCount).toDouble / requestCount * 100).round
          else 100}
      }"""
      }
      .mkString(",")

    s"""{
      "requestMetrics": { $pathMetrics },
      "timestamp": "${Instant.now()}"
    }"""
  }

  /**
   * Clear old metrics (call periodically to prevent memory leaks)
   */
  def cleanupOldMetrics(): Unit = {
    // Keep only recent data for performance
    requestTimes.clear()
    errorCounts.clear()
  }
}
