ThisBuild / version := "0.1.0"
ThisBuild / organization := "com.vitthalmirji"
ThisBuild / scalaVersion := "2.13.16"
ThisBuild / crossScalaVersions := Seq("2.13.16", "3.7.1")

// Publishing settings for eventual Maven Central release
ThisBuild / homepage := Some(url("https://github.com/vim89/valmuri"))
ThisBuild / licenses := List("MIT" -> url("https://opensource.org/licenses/MIT"))
ThisBuild / developers := List(
  Developer(
    "vim89",
    "Vitthal Mirji",
    "contact@vitthalmirji.com",
    url("https://github.com/vim89")
  )
)

// Common settings
Test / javaOptions += s"-Dproject.root=${(ThisBuild / baseDirectory).value.getAbsolutePath}"

lazy val commonSettings = Seq(
  scalacOptions ++= Seq(
    "-deprecation",
    "-feature",
    "-unchecked",
    "-encoding", "UTF-8",
    "-Xfatal-warnings"
  ),
  javacOptions ++= Seq("-source", "21", "-target", "21"),
  Test / fork := true,
  libraryDependencies ++= Seq(
    "org.scalameta" %% "munit" % "1.1.1" % Test
  ),
  testFrameworks += new TestFramework("munit.Framework")
)

// Core framework
lazy val core = (project in file("core"))
  .settings(commonSettings)
  .settings(
    name := "valmuri-core",
    description := "Valmuri framework core - Full-stack Scala web framework",
  )

// CLI tools
lazy val cli = (project in file("cli"))
  .dependsOn(core)
  .settings(commonSettings)
  .settings(
    name := "valmuri-cli",
    description := "Valmuri CLI tools for project generation and development",
    libraryDependencies ++= Seq(
      "com.github.scopt" %% "scopt" % "4.1.0"
    ),
    // Assembly settings for CLI executable
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case "application.conf" => MergeStrategy.concat
      case "reference.conf" => MergeStrategy.concat
      case x => MergeStrategy.first
    },
    assembly / mainClass := Some("com.vitthalmirji.valmuri.cli.ValmuriCLI"),
    assembly / assemblyJarName := "valmuri-cli.jar"
  )

// Examples
lazy val examples = (project in file("examples"))
  .dependsOn(core)
  .settings(commonSettings)
  .settings(
    name := "valmuri-examples",
    description := "Valmuri framework example applications",
    publish / skip := true,
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case x => MergeStrategy.first
    }
  )

// Root project
lazy val valmuri = (project in file("."))
  .aggregate(core, cli, examples)
  .settings(commonSettings)
  .settings(
    name := "valmuri",
    publish / skip := true,

    // Custom tasks for development workflow
    addCommandAlias("buildAll", ";core/compile;cli/compile;examples/compile"),
    addCommandAlias("testAll", ";core/test;cli/test;examples/test"),
    addCommandAlias("assemblyAll", ";cli/assembly;examples/assembly"),
    addCommandAlias("releaseAll", ";buildAll;testAll;assemblyAll")
  )
