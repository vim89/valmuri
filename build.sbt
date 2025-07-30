// build.sbt - Valmuri Framework Cross-Scala Build

ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.vitthalmirji"
ThisBuild / scalaVersion := "2.13.16"
ThisBuild / crossScalaVersions := Seq("2.13.16", "3.3.3")

// Publishing settings
ThisBuild / homepage := Some(url("https://github.com/vim89/valmuri"))
ThisBuild / licenses := List("MIT" -> url("https://opensource.org/licenses/MIT"))
ThisBuild / developers := List(
  Developer(
    "vim89",
    "Vitthal Mirji",
    "your-email@domain.com",
    url("https://github.com/vim89")
  )
)
ThisBuild / versionScheme := Some("early-semver")

// Common settings for all modules
lazy val commonSettings = Seq(
  scalacOptions ++= {
    val common = Seq(
      "-deprecation",
      "-feature",
      "-unchecked",
      "-encoding", "UTF-8"
    )

    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 13)) => common ++ Seq(
        "-Xfatal-warnings",
        "-language:higherKinds",
        "-language:implicitConversions",
        "-Xlint:_",
        "-Ywarn-value-discard",
        "-Ywarn-numeric-widen",
        "-Ywarn-dead-code"
      )
      case Some((3, _)) => common ++ Seq(
        "-Xfatal-warnings",
        "-language:strictEquality",
        "-Wunused:all"
      )
      case _ => common
    }
  },
  javacOptions ++= Seq("-source", "21", "-target", "21"),
  semanticdbEnabled := true,
  semanticdbVersion := scalafixSemanticdb.revision,
  Test / fork := true,
  Test / parallelExecution := true
)

// Common dependencies
lazy val zioDeps = Seq(
  "dev.zio" %% "zio" % "2.1.20",
  "dev.zio" %% "zio-streams" % "2.1.20",
  "dev.zio" %% "zio-http" % "3.3.3",
  "dev.zio" %% "zio-config" % "4.0.4",
  "dev.zio" %% "zio-config-typesafe" % "4.0.4",
  "dev.zio" %% "zio-config-magnolia" % "4.0.4",
  "dev.zio" %% "zio-logging" % "2.5.1",
  "dev.zio" %% "zio-json" % "0.7.44"
)

lazy val testDeps = Seq(
  "org.scalameta" %% "munit" % "1.1.1" % Test,
  "dev.zio" %% "zio-test" % "2.1.20" % Test,
  "dev.zio" %% "zio-test-sbt" % "2.1.20" % Test,
  "dev.zio" %% "zio-test-magnolia" % "2.1.20" % Test
)

// Root project - valmuri
lazy val valmuri = (project in file("."))
  .aggregate(core, cli, examples)
  .settings(commonSettings)
  .settings(
    name := "valmuri",
    publish / skip := true, // Don't publish the root project
    // Custom tasks
    addCommandAlias("compileAll", ";core/compile;cli/compile;examples/compile"),
    addCommandAlias("testAll", ";core/test;cli/test;examples/test"),
    addCommandAlias("buildAll", ";compileAll;testAll"),
    addCommandAlias("fmtAll", ";scalafmtAll;scalafmtSbt"),
    addCommandAlias("fixAll", ";scalafixAll;scalafmtAll")
  )

// Core framework module - valmuri-core
lazy val core = (project in file("core"))
  .settings(commonSettings)
  .settings(
    name := "valmuri-core",
    description := "Valmuri framework core - Modern scala full-stack framework",
    libraryDependencies ++= zioDeps ++ testDeps ++ Seq(
      "com.typesafe" % "config" % "1.4.4",
      "org.tpolecat" %% "doobie-core" % "1.0.0-RC10"
    ),
    // Test framework setup
    testFrameworks += new TestFramework("munit.Framework"),
    Test / testOptions += Tests.Argument(TestFrameworks.MUnit, "--verbose")
  )

// CLI module - valmuri-cli
lazy val cli = (project in file("cli"))
  .dependsOn(core)
  .settings(commonSettings)
  .settings(
    name := "valmuri-cli",
    description := "Valmuri framework CLI tools",
    libraryDependencies ++= testDeps ++ Seq(
      "com.github.scopt" %% "scopt" % "4.1.0"
    ),
    testFrameworks += new TestFramework("munit.Framework"),
    // Assembly settings for CLI executable
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case "application.conf" => MergeStrategy.concat
      case "reference.conf" => MergeStrategy.concat
      case x => MergeStrategy.first
    },
    assembly / mainClass := Some("valmuri.cli.ValmuriCLI"),
    assembly / assemblyJarName := "valmuri-cli.jar"
  )

// Examples module - valmuri-examples
lazy val examples = (project in file("examples"))
  .dependsOn(core)
  .settings(commonSettings)
  .settings(
    name := "valmuri-examples",
    description := "Valmuri framework examples",
    libraryDependencies ++= testDeps ++ Seq(
      "ch.qos.logback" % "logback-classic" % "1.4.14"
    ),
    testFrameworks += new TestFramework("munit.Framework"),
    publish / skip := true, // Don't publish examples
    // Runnable main classes
    Compile / mainClass := Some("HelloWorldApp"),
    // Multiple main classes for examples
    Compile / discoveredMainClasses := Seq(
      "HelloWorldApp",
      "ApiExampleApp"
    )
  )
