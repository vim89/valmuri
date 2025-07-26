// build.sc - Production-ready Mill configuration
import mill._, scalalib._, scalafmt._, publish._, scoverage._

val zioVersion = "2.1.22"
val zioHttpVersion = "3.0.4"
val zioConfigVersion = "4.0.6"
val munitVersion = "1.0.4"

trait CommonModule extends ScalaModule with ScalafmtModule {
  def scalaVersion = "2.13.12"

  def scalacOptions = Seq(
    "-deprecation",
    "-feature",
    "-unchecked",
    "-Xfatal-warnings",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-Ywarn-value-discard",
    "-Xlint",
    "-Wconf:any:error",
    "-Wunused:imports,privates,locals",
    "-Yrangepos"
  )

  def javacOptions = Seq("-source", "21", "-target", "21")
}

trait CommonTestModule extends ScalaModule with TestModule.Munit {
  def ivyDeps = Agg(
    ivy"org.scalameta::munit::$munitVersion",
    ivy"dev.zio::zio-test::$zioVersion",
    ivy"dev.zio::zio-test-sbt::$zioVersion",
    ivy"dev.zio::zio-test-magnolia::$zioVersion"
  )
}

object valmuri extends CommonModule with PublishModule with ScoverageModule {
  def artifactName = "valmuri-core"
  def publishVersion = "0.1.0-SNAPSHOT"

  def pomSettings = PomSettings(
    description = "Modern Scala web framework built on ZIO",
    organization = "com.vitthalmirji",
    url = "https://github.com/vitthalmirji/valmuri",
    licenses = Seq(License.MIT),
    versionControl = VersionControl.github("vitthalmirji", "valmuri"),
    developers = Seq(
      Developer("vitthalmirji", "Vitthal Mirji", "https://github.com/vitthalmirji")
    )
  )

  def ivyDeps = Agg(
    ivy"dev.zio::zio::$zioVersion",
    ivy"dev.zio::zio-http::$zioHttpVersion",
    ivy"dev.zio::zio-streams::$zioVersion",
    ivy"dev.zio::zio-config::$zioConfigVersion",
    ivy"dev.zio::zio-config-typesafe::$zioConfigVersion",
    ivy"dev.zio::zio-config-magnolia::$zioConfigVersion",
    ivy"dev.zio::zio-logging::2.3.5",
    ivy"dev.zio::zio-logging-slf4j2::2.3.5",
    ivy"ch.qos.logback:logback-classic:1.5.15",
    ivy"com.typesafe:config:1.4.3"
  )

  object test extends CommonTestModule with ScoverageTests {
    def moduleDeps = Seq(valmuri)
  }

  // Generate coverage reports
  def scoverageVersion = "2.2.0"
  def scoverageMinimumCoverage = 80.0
  def scoverageFailOnMinimumCoverage = true
}

object cli extends CommonModule {
  def moduleDeps = Seq(valmuri)
  def artifactName = "valmuri-cli"

  def ivyDeps = Agg(
    ivy"com.github.scopt::scopt::4.1.0",
    ivy"com.lihaoyi::os-lib::0.11.3"
  )

  def mainClass = Some("valmuri.cli.ValmuriCLI")

  object test extends CommonTestModule {
    def moduleDeps = Seq(cli, valmuri.test)
  }

  // Create executable script
  def assembly = T {
    val jar = super.assembly().path
    val scriptPath = millSourcePath / "valmuri"
    os.write(scriptPath, s"""#!/bin/bash
exec java -jar $jar "$@"
""")
    os.perms.set(scriptPath, "rwxr-xr-x")
    PathRef(jar)
  }
}

object examples extends Module {
  object helloWorld extends CommonModule {
    def moduleDeps = Seq(valmuri)
    def artifactName = "hello-world-example"
    def mainClass = Some("valmuri.hello.HelloWorldApp")

    object test extends CommonTestModule {
      def moduleDeps = Seq(helloWorld, valmuri.test)
    }
  }

  object fullCrud extends CommonModule {
    def moduleDeps = Seq(valmuri)
    def artifactName = "full-crud-example"

    def ivyDeps = Agg(
      ivy"org.xerial:sqlite-jdbc:3.47.1.0",
      ivy"org.tpolecat::doobie-core::1.0.0-RC11",
      ivy"org.tpolecat::doobie-hikari::1.0.0-RC11"
    )

    object test extends CommonTestModule {
      def moduleDeps = Seq(fullCrud, valmuri.test)
    }
  }
}

object benchmarks extends CommonModule {
  def moduleDeps = Seq(valmuri)

  def ivyDeps = Agg(
    ivy"org.openjdk.jmh:jmh-core:1.37",
    ivy"org.openjdk.jmh:jmh-generator-annprocess:1.37"
  )
}

// Development commands
def formatAll() = T.command {
  valmuri.reformat()()
  cli.reformat()()
  examples.helloWorld.reformat()()
}

def testAll() = T.command {
  valmuri.test.test()()
  cli.test.test()()
  examples.helloWorld.test.test()()
}

def coverage() = T.command {
  valmuri.scoverage.htmlReport()()
}

def publishLocal() = T.command {
  valmuri.publishLocal()()
}
