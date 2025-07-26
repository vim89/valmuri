import mill._, scalalib._, scalanativelib._

trait CommonScalaModule extends ScalaModule {
  def scalaVersion  = "2.13.12"
  def scalacOptions = Seq(
    "-deprecation",
    "-feature",
    "-unchecked",
    "-Xfatal-warnings",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-Ywarn-value-discard",
    "-Xlint",
  )
}

object valmuri extends CommonScalaModule {
  def ivyDeps = Agg(
    ivy"dev.zio::zio::2.1.22",
    ivy"dev.zio::zio-http::3.0.4",
    ivy"dev.zio::zio-config::4.0.6",
    ivy"dev.zio::zio-config-typesafe::4.0.6",
    ivy"dev.zio::zio-config-magnolia::4.0.6",
    ivy"dev.zio::zio-logging::2.3.5",
  )

  object test extends ScalaTests with TestModule.Munit {
    def ivyDeps = Agg(
      ivy"org.scalameta::munit::1.0.4",
      ivy"dev.zio::zio-test::2.1.22",
      ivy"dev.zio::zio-test-sbt::2.1.22",
    )
  }
}
