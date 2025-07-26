package valmuri.di

import zio._
import scala.reflect.ClassTag

trait ServiceRegistry {
  def register[A: ClassTag](name: String, layer: ZLayer[Any, Throwable, A]): UIO[Unit]
  def get[A: ClassTag](name: String): Task[A]
  def getAll[A: ClassTag]: Task[List[A]]
  def wire[A: ClassTag: Tag]: ZLayer[Any, Throwable, A]
}

object ServiceRegistry {
  case class ServiceEntry[A](
      name: String,
      serviceType: ClassTag[A],
      layer: ZLayer[Any, Throwable, A],
  )

  final case class ServiceRegistryLive(
      private val services: Ref[Map[String, ServiceEntry[?]]]
  ) extends ServiceRegistry {

    def register[A: ClassTag](name: String, layer: ZLayer[Any, Throwable, A]): UIO[Unit] =
      services.update(_ + (name -> ServiceEntry(name, implicitly[ClassTag[A]], layer)))

    def get[A: ClassTag](name: String): Task[A] =
      for {
        serviceMap <- services.get
        entry      <- ZIO
          .fromOption(serviceMap.get(name))
          .orElseFail(new RuntimeException(s"Service not found: $name"))
        service <- ZIO.scoped(entry.layer.build.map(_.get))
      } yield service.asInstanceOf[A]

    def getAll[A: ClassTag]: Task[List[A]] = {
      val targetType = implicitly[ClassTag[A]]
      for {
        serviceMap <- services.get
        matchingServices = serviceMap.values.filter(_.serviceType == targetType).toList
        services <- ZIO.foreach(matchingServices) { entry =>
          ZIO.scoped(entry.layer.build.map(_.get.asInstanceOf[A]))
        }
      } yield services
    }

    def wire[A: ClassTag: Tag]: ZLayer[Any, Throwable, A] = {
      val serviceName = implicitly[ClassTag[A]].runtimeClass.getSimpleName
      ZLayer.fromZIO(get[A](serviceName))
    }
  }

  val live: ZLayer[Any, Nothing, ServiceRegistry] =
    ZLayer.fromZIO(Ref.make(Map.empty[String, ServiceEntry[?]]).map(ServiceRegistryLive(_)))
}
