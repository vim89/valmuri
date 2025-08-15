package com.vitthalmirji.valmuri.di

import com.vitthalmirji.valmuri.error.VResult

import scala.collection.mutable
import scala.reflect.ClassTag
import scala.util.Try

/**
 * Lightweight dependency injection container
 */
class VServices {
  private val services  = mutable.Map[Class[?], Any]()
  private val factories = mutable.Map[Class[?], () => Any]()

  /**
   * Register a service instance
   */

  def register[T](service: T)(implicit classTag: ClassTag[T]): VResult[Unit] =
    VResult.fromTry(Try {
      val clazz = classTag.runtimeClass
      services(clazz) = service
      println(s"📦 Registered service: ${clazz.getSimpleName}")
      VResult.success(())
    })

  /**
   * Register a service factory (lazy instantiation)
   */
  def registerFactory[T: ClassTag](factory: => T): VResult[Unit] = {
    val clazz = implicitly[ClassTag[T]].runtimeClass
    factories(clazz) = () => factory
    VResult.success(())
  }

  /**
   * Get a service (creates from factory if needed)
   */
  def get[T: ClassTag]: T = {
    val clazz = implicitly[ClassTag[T]].runtimeClass

    services.get(clazz) match {
      case Some(service) =>
        service.asInstanceOf[T]

      case None =>
        factories.get(clazz) match {
          case Some(factory) =>
            val service = factory().asInstanceOf[T]
            services(clazz) = service
            service

          case None =>
            throw new RuntimeException(s"Service not found: ${clazz.getSimpleName}")
        }
    }
  }

  /**
   * Safe get with VResult
   */

  def getSafe[T](implicit classTag: ClassTag[T]): VResult[T] =
    VResult.fromTry(Try(get[T]))

  /**
   * Check if service exists
   */
  def contains[T: ClassTag]: Boolean = {
    val clazz = implicitly[ClassTag[T]].runtimeClass
    services.contains(clazz) || factories.contains(clazz)
  }

  /**
   * Simple auto-wiring (constructor injection)
   */
  def autowire[T: ClassTag]: VResult[T] = {
    val clazz = implicitly[ClassTag[T]].runtimeClass

    VResult.fromTry(scala.util.Try {
      val constructor = clazz.getConstructors.headOption.getOrElse(
        throw new RuntimeException(s"No constructor found for ${clazz.getSimpleName}")
      )

      val paramTypes = constructor.getParameterTypes

      if (paramTypes.isEmpty) {
        constructor.newInstance().asInstanceOf[T]
      } else {
        val params = paramTypes.map { paramType =>
          services.getOrElse(paramType, throw new RuntimeException(s"Dependency not found: ${paramType.getSimpleName}"))
        }
        constructor.newInstance(params: _*).asInstanceOf[T]
      }
    })
  }

  /**
   * List all registered services
   */
  def listServices(): List[String] =
    (services.keys ++ factories.keys).map(_.getSimpleName).toList.sorted
}
