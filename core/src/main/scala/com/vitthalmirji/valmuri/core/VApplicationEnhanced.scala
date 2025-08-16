package com.vitthalmirji.valmuri.core

import com.vitthalmirji.valmuri.config.types.VConfigExtended
import com.vitthalmirji.valmuri.di.ServiceRegistry
import com.vitthalmirji.valmuri.error.VResult

import scala.reflect.ClassTag

/**
 * Enhanced application trait that extends your existing VApplication
 * Provides additional features while maintaining full backward compatibility
 */
trait VApplicationEnhanced extends VApplication {

  // Enhanced service registry that wraps your existing VServices
  private lazy val enhancedServices: ServiceRegistry = new ServiceRegistry(services)

  // Enhanced configuration access
  private lazy val enhancedConfig: VConfigExtended = VConfigExtended.fromVConfig(config)

  /**
   * Enhanced service access with lifecycle checking
   * Falls back to existing service method if enhanced features not needed
   */
  protected def serviceWithLifecycle[T: ClassTag]: VResult[T] =
    enhancedServices.getWithLifecycleCheck[T]

  /**
   * Register service with enhanced lifecycle tracking
   * This is additive - your existing service registration still works
   */
  protected def registerServiceWithLifecycle[T: ClassTag](service: T): VResult[Unit] =
    enhancedServices.registerWithLifecycle(service)

  /**
   * Access enhanced configuration while keeping original config working
   */
  def getEnhancedConfig: VConfigExtended = enhancedConfig

  /**
   * Enhanced framework initialization that builds on your existing initializeFramework
   * This is called after your existing initialization
   */
  private def enhancedInitialization(): VResult[Unit] =
    for {
      _ <- VResult.success(println("🔧 Starting enhanced initialization..."))
      _ <- enhancedServices.validateDependencies()
      _ <- registerFrameworkServices()
      _ <- VResult.success(println("✅ Enhanced initialization completed"))
    } yield ()

  /**
   * Register additional framework services that enhance functionality
   */
  private def registerFrameworkServices(): VResult[Unit] =
    for {
      _ <- enhancedServices.register(enhancedConfig)
      _ <- enhancedServices.register(enhancedConfig.server)
      _ <- enhancedServices.register(enhancedConfig.actuator)
      _ <- enhancedServices.register(enhancedConfig.cors)
      _ <- enhancedServices.register(enhancedConfig.database)
    } yield ()

  /**
   * Override start method to add enhanced initialization
   * Calls your existing start method first, then adds enhancements
   */
  final override def start(): VResult[Unit] =
    for {
      _ <- super.start()            // Call your existing start method
      _ <- enhancedInitialization() // Add enhanced features
    } yield ()

  /**
   * Debugging helper - shows service states
   */
  protected def debugServices(): Unit = {
    println("=== Service Registry Debug ===")
    enhancedServices.getServiceStates.foreach { case (name, state) =>
      println(s"  $name -> $state")
    }
    println("  Registered services:")
    enhancedServices.listServices().foreach(name => println(s"    ✓ $name"))
    println("==============================")
  }
}
