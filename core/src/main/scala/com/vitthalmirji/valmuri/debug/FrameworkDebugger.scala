package com.vitthalmirji.valmuri.debug

import com.vitthalmirji.valmuri.config.types.VConfigExtended
import com.vitthalmirji.valmuri.di.ServiceRegistry

/**
 * Debugging utilities for framework development and troubleshooting
 * Helps developers understand what's happening inside the framework
 */
class FrameworkDebugger(
  config: VConfigExtended,
  serviceRegistry: ServiceRegistry
) {

  /**
   * Print comprehensive framework state
   */
  def printFrameworkState(): Unit = {
    println("=" * 60)
    println("🔍 VALMURI FRAMEWORK DEBUG STATE")
    println("=" * 60)

    printConfigurationState()
    printServiceState()
    printSystemState()

    println("=" * 60)
  }

  private def printConfigurationState(): Unit = {
    println("\n📋 CONFIGURATION STATE:")
    println(s"  Application: ${config.appName} v${config.appVersion}")
    println(s"  Profile: ${config.profile}")
    println(s"  Server: ${config.server.host}:${config.server.port}")
    println(s"  Threads: ${config.server.threads}")
    println(s"  Actuator: ${if (config.actuator.enabled) "ENABLED" else "DISABLED"}")
    println(s"  CORS: ${if (config.cors.enabled) "ENABLED" else "DISABLED"}")
    println(s"  Static Files: ${config.base.staticDir.getOrElse("NONE")}")
    println(s"  Database: ${config.database.url.getOrElse("NONE")}")
  }

  private def printServiceState(): Unit = {
    println("\n📦 SERVICE REGISTRY STATE:")
    val services = serviceRegistry.listServices()
    if (services.nonEmpty) {
      services.foreach(service => println(s"  ✓ $service"))
    } else {
      println("  (No services registered)")
    }

    val states = serviceRegistry.getServiceStates
    if (states.nonEmpty) {
      println("\n  Service Lifecycle States:")
      states.foreach { case (name, state) =>
        val icon = state match {
          case "Ready"        => "✅"
          case "Registered"   => "📋"
          case "Initializing" => "⏳"
          case "Failed"       => "❌"
          case _              => "❓"
        }
        println(s"    $icon $name -> $state")
      }
    }
  }

  private def printSystemState(): Unit = {
    val runtime     = Runtime.getRuntime
    val maxMemory   = runtime.maxMemory()
    val totalMemory = runtime.totalMemory()
    val freeMemory  = runtime.freeMemory()
    val usedMemory  = totalMemory - freeMemory

    println("\n💻 SYSTEM STATE:")
    println(s"  Java Version: ${System.getProperty("java.version")}")
    println(s"  Available Processors: ${runtime.availableProcessors()}")
    println(
      s"  Memory Usage: ${formatBytes(usedMemory)} / ${formatBytes(maxMemory)} (${(usedMemory.toDouble / maxMemory * 100).round}%)"
    )
    println(s"  Active Threads: ${Thread.activeCount()}")
  }

  private def formatBytes(bytes: Long): String = {
    val units     = Array("B", "KB", "MB", "GB", "TB")
    var size      = bytes.toDouble
    var unitIndex = 0

    while (size >= 1024 && unitIndex < units.length - 1) {
      size /= 1024
      unitIndex += 1
    }

    f"$size%.1f ${units(unitIndex)}"
  }

  /**
   * Validate framework setup and report issues
   */
  def validateSetup(): List[String] = {
    val issues = scala.collection.mutable.ListBuffer[String]()

    // Validate configuration
    if (!config.server.isValidPort) {
      issues += s"Invalid server port: ${config.server.port}"
    }

    if (!config.server.isValidHost) {
      issues += s"Invalid server host: ${config.server.host}"
    }

    // Validate services
    val services = serviceRegistry.listServices()
    if (services.isEmpty) {
      issues += "No services registered - application may not function properly"
    }

    // Check memory
    val runtime           = Runtime.getRuntime
    val usedMemoryPercent = (runtime.totalMemory() - runtime.freeMemory()).toDouble / runtime.maxMemory() * 100
    if (usedMemoryPercent > 90) {
      issues += f"High memory usage: $usedMemoryPercent%.1f%%"
    }

    issues.toList
  }
}
