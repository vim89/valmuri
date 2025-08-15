package com.vitthalmirji.valmuri

import java.lang.management.ManagementFactory
import java.time.Instant

/**
 * Production-ready endpoints like Spring Boot Actuator
 */
class VActuator {
  private val startTime = Instant.now()

  def healthEndpoint(): String = {
    val uptime   = java.time.Duration.between(startTime, Instant.now())
    val memory   = ManagementFactory.getMemoryMXBean
    val heapUsed = memory.getHeapMemoryUsage.getUsed / (1024 * 1024)
    val heapMax  = memory.getHeapMemoryUsage.getMax / (1024 * 1024)

    s"""{
      "status": "UP",
      "system": {
        "uptime": "${uptime.toSeconds}s",
        "memory": {
          "used": "${heapUsed}MB",
          "max": "${heapMax}MB"
        },
        "processors": ${Runtime.getRuntime.availableProcessors()}
      }
    }"""
  }

  def metricsEndpoint(): String = {
    val runtime = ManagementFactory.getRuntimeMXBean
    val memory  = ManagementFactory.getMemoryMXBean

    s"""{
      "jvm": {
        "uptime": ${runtime.getUptime},
        "memory": {
          "heap_used": ${memory.getHeapMemoryUsage.getUsed},
          "heap_max": ${memory.getHeapMemoryUsage.getMax}
        }
      }
    }"""
  }

  def infoEndpoint(): String =
    s"""{
      "app": {
        "name": "Valmuri Application",
        "version": "0.1.0",
        "framework": "Valmuri"
      },
      "build": {
        "java": "${System.getProperty("java.version")}",
        "scala": "2.13.12"
      }
    }"""
}
