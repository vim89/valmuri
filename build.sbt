// build.sbt (updated with correct dependencies)
ThisBuild / scalaVersion := "2.13.12"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.vitthalmirji"

ThisBuild / crossScalaVersions := Seq("2.13.12")

ThisBuild / scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Xfatal-warnings",
  "-encoding", "UTF-8",
  "-language:higherKinds",
  "-language:implicitConversions"
)

lazy val commonSettings = Seq(
  Test / fork := false,
  Compile / fork := true,
  testFrameworks += new TestFramework("munit.Framework")
)

lazy val valmuri = project
  .in(file("."))
  .settings(commonSettings)
  .settings(
    name := "valmuri",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio-logging" % "2.1.14",
      "com.typesafe" % "config" % "1.4.3",

      // Testing
      "org.scalameta" %%% "munit" % "1.0.0-M10" % Test,
      "dev.zio" %% "zio-test" % "2.1.20" % Test,
      "dev.zio" %% "zio-test-sbt" % "2.1.20" % Test,

      "dev.zio" %% "zio" % "2.1.20",
      "dev.zio" %% "zio-http" % "3.3.3",
      "dev.zio" %% "zio-streams" % "2.1.20",
      "dev.zio" %% "zio-config" % "4.0.4",
      "dev.zio" %% "zio-config-typesafe" % "4.0.4",
      "dev.zio" %% "zio-config-magnolia" % "4.0.4",
      "io.circe" %% "circe-generic" % "0.14.14",
      "org.tpolecat" %% "doobie-core" % "1.0.0-RC10",
      "com.github.scopt" %% "scopt" % "4.1.0",
      "org.scalameta" %%% "munit" % "1.0.0-M10" % Test
    ),
    Compile / scalaSource := baseDirectory.value / "src/main/scala",
    Test / scalaSource := baseDirectory.value / "src/test/scala",
    Compile / resourceDirectory := baseDirectory.value / "src/main/resources"
  )

lazy val cli = project
  .in(file("cli"))
  .dependsOn(valmuri)
  .settings(commonSettings)
  .settings(
    name := "valmuri-cli",
    libraryDependencies ++= Seq("com.github.scopt" %% "scopt" % "4.1.0"),
    Compile / mainClass := Some("valmuri.cli.ValmuriCLI"),
    assembly / assemblyJarName := "valmuri-cli.jar"
  )

lazy val helloWorld = project
  .in(file("examples/hello-world-example"))
  .dependsOn(valmuri)
  .settings(commonSettings)
  .settings(
    name := "hello-world-example",
    libraryDependencies := Seq.empty,
    Compile / mainClass := Some("valmuri.hello.HelloWorldApp"),
    publish / skip := true
  )

// Command aliases
addCommandAlias("buildAll", ";clean;compile;test;cli/compile;helloWorld/compile")
addCommandAlias("runExample", "helloWorld/run")
addCommandAlias("testFramework", "test")
addCommandAlias("dev", ";~compile;test")
