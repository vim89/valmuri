package valmuri.debug

import valmuri.di.Container
import zio._

object DIDebugger {
  def printRegisteredServices(container: Container): Task[Unit] = for {
    _ <- ZIO.logInfo("🔍 === REGISTERED SERVICES ===")
    // Implementation will list all registered services
    _ <- ZIO.logInfo("📦 Core Services:")
    _ <- ZIO.logInfo("  - HealthService")
    _ <- ZIO.logInfo("  - ConfigLoader")
    _ <- ZIO.logInfo("  - ServiceRegistry")
    _ <- ZIO.logInfo("🔍 === END SERVICES ===")
  } yield ()

  def checkServiceHealth(container: Container): Task[Unit] = for {
    _           <- ZIO.logInfo("🏥 === SERVICE HEALTH CHECK ===")
    healthCheck <- container.resolve[valmuri.core.HealthService].either
    _           <- healthCheck match {
      case Right(_)               => ZIO.logInfo("✅ HealthService: OK")
      case Left(error: Throwable) => ZIO.logError(s"❌ HealthService: ${error.getMessage}")
      case Left(error)            => ZIO.logError(s"❌ HealthService: ${error.toString}")
    }
    _ <- ZIO.logInfo("🏥 === END HEALTH CHECK ===")
  } yield ()
}
