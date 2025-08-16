package com.vitthalmirji.valmuri.actuator

import com.vitthalmirji.valmuri.config.types.VConfigExtended

import java.time.Instant
import java.time.format.DateTimeFormatter
import scala.jdk.CollectionConverters.{ CollectionHasAsScala, MapHasAsScala }

/**
 * Enhanced actuator that provides detailed application insights
 * Works with your existing actuator routes in VApplication
 */
class VActuator(config: VConfigExtended) {

  private val startTime          = Instant.now()
  private var requestCount: Long = 0
  private var errorCount: Long   = 0

  /**
   * Enhanced health check with detailed status
   * This replaces your existing healthCheck() method with more details
   */
  def healthEndpoint(): String = {
    val uptime      = java.time.Duration.between(startTime, Instant.now())
    val runtime     = Runtime.getRuntime
    val maxMemory   = runtime.maxMemory()
    val totalMemory = runtime.totalMemory()
    val freeMemory  = runtime.freeMemory()
    val usedMemory  = totalMemory - freeMemory

    s"""{
      "status": "UP",
      "timestamp": "${DateTimeFormatter.ISO_INSTANT.format(Instant.now())}",
      "uptime": {
        "seconds": ${uptime.getSeconds},
        "human": "${formatDuration(uptime)}"
      },
      "memory": {
        "used": $usedMemory,
        "free": $freeMemory,
        "total": $totalMemory,
        "max": $maxMemory,
        "usedPercent": ${(usedMemory.toDouble / maxMemory * 100).round}
      },
      "application": {
        "name": "${config.appName}",
        "version": "${config.appVersion}",
        "profile": "${config.profile}"
      },
      "server": {
        "host": "${config.server.host}",
        "port": ${config.server.port},
        "threads": ${config.server.threads}
      }
    }"""
  }

  /**
   * Enhanced metrics with request tracking
   * This replaces your existing metrics() method
   */
  def metricsEndpoint(): String = {
    val runtime = Runtime.getRuntime
    val gc      = java.lang.management.ManagementFactory.getGarbageCollectorMXBeans

    val gcInfo = gc.asScala
      .map { gcBean =>
        s""""${gcBean.getName}": {
        "collections": ${gcBean.getCollectionCount},
        "time": ${gcBean.getCollectionTime}
      }"""
      }
      .mkString(",")

    s"""{
      "requests": {
        "total": $requestCount,
        "errors": $errorCount,
        "successRate": ${if (requestCount > 0) ((requestCount - errorCount).toDouble / requestCount * 100).round
      else 100}
      },
      "jvm": {
        "memory": {
          "heap": {
            "used": ${runtime.totalMemory() - runtime.freeMemory()},
            "max": ${runtime.maxMemory()},
            "committed": ${runtime.totalMemory()}
          }
        },
        "gc": { $gcInfo },
        "threads": {
          "active": ${Thread.activeCount()},
          "peak": ${java.lang.management.ManagementFactory.getThreadMXBean.getPeakThreadCount}
        }
      },
      "system": {
        "processors": ${runtime.availableProcessors()},
        "loadAverage": ${java.lang.management.ManagementFactory.getOperatingSystemMXBean.getSystemLoadAverage}
      }
    }"""
  }

  /**
   * Enhanced info endpoint with configuration details
   * This replaces your existing info() method
   */
  def infoEndpoint(): String = {
    val buildTime = getClass.getPackage.getImplementationVersion

    s"""{
      "application": {
        "name": "${config.appName}",
        "version": "${config.appVersion}",
        "profile": "${config.profile}",
        "started": "${DateTimeFormatter.ISO_INSTANT.format(startTime)}"
      },
      "build": {
        "version": "${Option(buildTime).getOrElse("development")}",
        "time": "${DateTimeFormatter.ISO_INSTANT.format(startTime)}"
      },
      "server": {
        "host": "${config.server.host}",
        "port": ${config.server.port},
        "threads": ${config.server.threads},
        "backlog": ${config.server.backlog}
      },
      "features": {
        "actuator": ${config.actuator.enabled},
        "cors": ${config.cors.enabled},
        "staticFiles": ${config.base.staticDir.isDefined},
        "templates": ${config.base.templateDir.isDefined},
        "database": ${config.database.url.isDefined}
      },
      "java": {
        "version": "${System.getProperty("java.version")}",
        "vendor": "${System.getProperty("java.vendor")}",
        "home": "${System.getProperty("java.home")}"
      },
      "os": {
        "name": "${System.getProperty("os.name")}",
        "version": "${System.getProperty("os.version")}",
        "arch": "${System.getProperty("os.arch")}"
      }
    }"""
  }

  /**
   * Track request metrics (call this from your VServer)
   */
  def recordRequest(success: Boolean = true): Unit = {
    requestCount += 1
    if (!success) errorCount += 1
  }

  /**
   * Environment endpoint showing configuration
   */
  def environmentEndpoint(): String = {
    val envVars = System
      .getenv()
      .asScala
      .filter { case (key, _) => key.startsWith("VALMURI_") || key.startsWith("JAVA_") }
      .map { case (key, value) => s""""$key": "$value"""" }
      .mkString(",")

    val systemProps = System.getProperties.asScala
      .filter { case (key, _) => key.toString.startsWith("valmuri.") || key.toString.startsWith("java.") }
      .map { case (key, value) => s""""$key": "$value"""" }
      .mkString(",")

    s"""{
      "environment": { $envVars },
      "systemProperties": { $systemProps },
      "configuration": {
        "profile": "${config.profile}",
        "serverPort": ${config.server.port},
        "actuatorEnabled": ${config.actuator.enabled},
        "corsEnabled": ${config.cors.enabled}
      }
    }"""
  }

  private def formatDuration(duration: java.time.Duration): String = {
    val days    = duration.toDays
    val hours   = duration.toHours    % 24
    val minutes = duration.toMinutes  % 60
    val seconds = duration.getSeconds % 60

    if (days > 0) s"${days}d ${hours}h ${minutes}m ${seconds}s"
    else if (hours > 0) s"${hours}h ${minutes}m ${seconds}s"
    else if (minutes > 0) s"${minutes}m ${seconds}s"
    else s"${seconds}s"
  }
}
