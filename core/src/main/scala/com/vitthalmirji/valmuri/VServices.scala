package com.vitthalmirji.valmuri

import scala.collection.mutable
import scala.reflect.ClassTag

/**
 * Simple dependency injection - Spring Boot style
 */
class VServices {
  private val services = mutable.Map[Class[_], Any]()

  def register[T](service: T)(implicit classTag: ClassTag[T]): Unit = {
    val clazz = classTag.runtimeClass
    services(clazz) = service
    println(s"📦 Registered service: ${clazz.getSimpleName}")
  }

  def get[T](implicit classTag: ClassTag[T]): T = {
    val clazz = classTag.runtimeClass
    services.get(clazz) match {
      case Some(service) => service.asInstanceOf[T]
      case None => throw new RuntimeException(s"Service not found: ${clazz.getSimpleName}")
    }
  }

  def autowire[T](implicit classTag: ClassTag[T]): T = {
    val clazz = classTag.runtimeClass
    val constructor = clazz.getConstructors.head
    val paramTypes = constructor.getParameterTypes

    if (paramTypes.isEmpty) {
      constructor.newInstance().asInstanceOf[T]
    } else {
      val params = paramTypes.map { paramType =>
        services.get(paramType) match {
          case Some(service) => service
          case None => throw new RuntimeException(
            s"Cannot autowire ${clazz.getSimpleName}: missing dependency ${paramType.getSimpleName}"
          )
        }
      }
      constructor.newInstance(params: _*).asInstanceOf[T]
    }
  }
}
