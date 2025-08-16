package com.vitthalmirji.valmuri.di

import com.vitthalmirji.valmuri.error.VResult
import com.vitthalmirji.valmuri.error.ValmuriError.ServiceError

import scala.reflect.ClassTag
import scala.collection.mutable
import scala.util.Try

/**
 * Extended service registry that works with existing VServices
 * Adds advanced features without breaking existing functionality
 */

class ServiceRegistry(baseServices: VServices) {

  // Track service lifecycle and dependencies
  private val serviceLifecycle    = mutable.Map[Class[?], ServiceState]()
  private val serviceDependencies = mutable.Map[Class[?], List[Class[?]]]()

  // Service states for lifecycle management
  sealed trait ServiceState
  case object NotRegistered extends ServiceState
  case object Registered    extends ServiceState
  case object Initializing  extends ServiceState
  case object Ready         extends ServiceState
  case object Failed        extends ServiceState

  /**
   * Enhanced service registration with lifecycle tracking
   * Wraps your existing VServices.register method
   */
  def registerWithLifecycle[T: ClassTag](service: T): VResult[Unit] = {
    val clazz = implicitly[ClassTag[T]].runtimeClass

    for {
      _ <- VResult.fromTry(Try {
        serviceLifecycle(clazz) = Registered
        println(s"📦 Lifecycle: ${clazz.getSimpleName} -> Registered")
      })
      _ <- baseServices.register(service) // Use existing VServices
      _ <- VResult.fromTry(Try {
        serviceLifecycle(clazz) = Ready
        println(s"📦 Lifecycle: ${clazz.getSimpleName} -> Ready")
      })
    } yield ()
  }

  /**
   * Enhanced factory registration with dependency tracking
   */
  def registerFactoryWithDependencies[T: ClassTag](
    factory: => T,
    dependencies: List[Class[?]] = List.empty
  ): VResult[Unit] = {
    val clazz = implicitly[ClassTag[T]].runtimeClass

    for {
      _ <- VResult.fromTry(Try {
        serviceDependencies(clazz) = dependencies
        serviceLifecycle(clazz) = Registered
      })
      _ <- baseServices.registerFactory(factory) // Use existing VServices
      _ <- VResult.fromTry(Try {
        serviceLifecycle(clazz) = Ready
      })
    } yield ()
  }

  /**
   * Get service with lifecycle checking
   * Wraps your existing VServices.get method
   */
  def getWithLifecycleCheck[T: ClassTag]: VResult[T] = {
    val clazz = implicitly[ClassTag[T]].runtimeClass

    serviceLifecycle.get(clazz) match {
      case Some(Ready)  => baseServices.getSafe[T]
      case Some(Failed) => VResult.failure(ServiceError(s"Service ${clazz.getSimpleName} is in failed state"))
      case Some(Initializing) =>
        VResult.failure(ServiceError(s"Service ${clazz.getSimpleName} is still initializing"))
      case Some(Registered) =>
        VResult
          .fromTry(Try {
            serviceLifecycle(clazz) = Initializing
            println(s"⚙️ Initializing service: ${clazz.getSimpleName}")
          })
          .flatMap { _ =>
            baseServices
              .getSafe[T]
              .map { instance =>
                serviceLifecycle(clazz) = Ready
                println(s"✅ Service ${clazz.getSimpleName} is Ready")
                instance
              }
              .recoverWith { err =>
                serviceLifecycle(clazz) = Failed
                VResult.failure(
                  ServiceError(
                    s"Service ${clazz.getSimpleName} initialization failed: ${err.message}"
                  )
                )
              }
          }
      case Some(NotRegistered) =>
        VResult.failure(ServiceError(s"Service ${clazz.getSimpleName} is not registered"))
      case None => baseServices.getSafe[T] // Fall back to existing behavior
    }
  }

  /**
   * Check circular dependencies before service creation
   */
  def validateDependencies(): VResult[Unit] =
    VResult.fromTry(Try {
      def checkCircular(clazz: Class[_], visited: Set[Class[_]]): Unit = {
        if (visited.contains(clazz)) {
          val cycle = visited.toList.map(_.getSimpleName).mkString(" -> ")
          throw new RuntimeException(s"Circular dependency detected: $cycle -> ${clazz.getSimpleName}")
        }

        serviceDependencies.get(clazz) match {
          case Some(deps) => deps.foreach(dep => checkCircular(dep, visited + clazz))
          case None       => // No dependencies to check
        }
      }

      serviceDependencies.keys.foreach(clazz => checkCircular(clazz, Set.empty))
      println("✅ Dependency validation passed - no circular dependencies")
    })

  /**
   * Get service states for debugging
   */
  def getServiceStates: Map[String, String] =
    serviceLifecycle.map { case (clazz, state) =>
      clazz.getSimpleName -> state.toString
    }.toMap

  /**
   * Delegate all existing VServices methods for compatibility
   */
  def register[T: ClassTag](service: T): VResult[Unit] = baseServices.register(service)
  def get[T: ClassTag]: T                              = baseServices.get[T]
  def getSafe[T: ClassTag]: VResult[T]                 = baseServices.getSafe[T]
  def contains[T: ClassTag]: Boolean                   = baseServices.contains[T]
  def listServices(): List[String]                     = baseServices.listServices()
}
