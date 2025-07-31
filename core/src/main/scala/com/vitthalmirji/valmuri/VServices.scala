package com.vitthalmirji.valmuri

import scala.collection.mutable
import scala.reflect.ClassTag
import scala.util.{Try, Success, Failure}

/**
 * Enhanced dependency injection - Fixed for compilation
 */
class VServices {
  private val services = mutable.Map[Class[_], Any]()

  // Type alias for service factories
  type ServiceFactory[T] = () => T

  /**
   * Register a service instance with type safety
   */
  def register[T](service: T)(implicit classTag: ClassTag[T]): VResult[Unit] = {
    VResult.fromTry(Try {
      val clazz = classTag.runtimeClass
      services(clazz) = service
      println(s"📦 Registered service: ${clazz.getSimpleName}")
    })
  }

  /**
   * Get service with pattern matching and error handling
   */
  def get[T](implicit classTag: ClassTag[T]): T = {
    val clazz = classTag.runtimeClass
    services.get(clazz) match {
      case Some(service) => service.asInstanceOf[T]
      case None => throw new RuntimeException(s"Service not found: ${clazz.getSimpleName}")
    }
  }

  /**
   * Safe get with VResult
   */
  def getSafe[T](implicit classTag: ClassTag[T]): VResult[T] = {
    VResult.fromTry(Try(get[T]))
  }

  /**
   * Get optional service (returns Option)
   */
  def getOption[T](implicit classTag: ClassTag[T]): Option[T] = {
    Try(get[T]).toOption
  }

  /**
   * Enhanced auto-wiring with better error handling
   */
  def autowire[T](implicit classTag: ClassTag[T]): T = {
    val clazz = classTag.runtimeClass

    clazz.getConstructors.headOption match {
      case Some(constructor) =>
        val paramTypes = constructor.getParameterTypes

        if (paramTypes.isEmpty) {
          constructor.newInstance().asInstanceOf[T]
        } else {
          autowireDependencies(constructor, paramTypes) match {
            case Right(instance) => instance.asInstanceOf[T]
            case Left(error) => throw error
          }
        }
      case None =>
        throw new RuntimeException(s"No constructor found for ${clazz.getSimpleName}")
    }
  }

  /**
   * Auto-wire dependencies using Either for error handling
   */
  private def autowireDependencies(
                                    constructor: java.lang.reflect.Constructor[_],
                                    paramTypes: Array[Class[_]]
                                  ): Either[RuntimeException, Any] = {
    Try {
      val params = paramTypes.map { paramType =>
        services.get(paramType) match {
          case Some(service) => service
          case None =>
            throw new RuntimeException(
              s"Cannot autowire: missing dependency ${paramType.getSimpleName}"
            )
        }
      }
      constructor.newInstance(params: _*)
    } match {
      case Success(instance) => Right(instance)
      case Failure(ex) => Left(new RuntimeException(ex.getMessage, ex))
    }
  }

  /**
   * Check if service is registered
   */
  def contains[T](implicit classTag: ClassTag[T]): Boolean = {
    services.contains(classTag.runtimeClass)
  }

  /**
   * Get service count
   */
  def serviceCount: Int = services.size

  /**
   * List all registered service types
   */
  def listServices: List[String] = {
    services.keys.map(_.getSimpleName).toList.sorted
  }
}
