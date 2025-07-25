// Valmuri Framework - build.sbt (refactored)

ThisBuild / scalaVersion := "2.13.12"
ThisBuild / version      := "0.1.0-SNAPSHOT"
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
  testFrameworks += new TestFramework("munit.Framework"),
  publishMavenStyle := true,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  }
)

lazy val valmuri = project
  .in(file("."))
  .settings(commonSettings)
  .settings(
    name := "valmuri",
    libraryDependencies ++= Seq(
      "dev.zio"        %% "zio"           % "2.1.20",    // latest stable patch  [oai_citation:0‡github.com](https://github.com/zio/zio/releases?utm_source=chatgpt.com)
      "dev.zio"        %% "zio-http"      % "3.3.3",     // stable official ZIO HTTP release  [oai_citation:1‡mvnrepository.com](https://mvnrepository.com/artifact/dev.zio/zio-http?utm_source=chatgpt.com)
      "dev.zio"        %% "zio-streams"   % "2.1.20",
      "io.circe"       %% "circe-generic" % "0.14.14",
      "org.tpolecat"   %% "doobie-core"   % "1.0.0-RC10",
      "com.github.scopt" %% "scopt"       % "4.1.0",
      "org.scalameta"  %%% "munit"        % "1.0.0-M10" % Test
    ),
    Compile / scalaSource := baseDirectory.value / "src/main/scala",
    Test / scalaSource    := baseDirectory.value / "src/test/scala",
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

lazy val examples = project
  .in(file("examples"))
  .dependsOn(helloWorld)
  .settings(
    name := "valmuri-examples",
    publish / skip := true
  )

lazy val docs = project
  .in(file("docs"))
  .dependsOn(valmuri)
  .settings(commonSettings)
  .settings(
    name := "valmuri-docs",
    libraryDependencies ++= Seq("com.lihaoyi" %%% "scalatags" % "0.12.0"),
    publish / skip := true
  )

lazy val root = project
  .in(file("."))
  .aggregate(valmuri, cli, helloWorld, docs)
  .settings(
    name := "valmuri",
    publish / skip := true
  )

addCommandAlias("buildAll", ";clean;valmuri/compile;valmuri/test;cli/compile;helloWorld/compile")
addCommandAlias("runExample", "helloWorld/run")
addCommandAlias("packageCli", "cli/assembly")
addCommandAlias("testFramework", "valmuri/test")
addCommandAlias("dev", ";~valmuri/compile;valmuri/test")
addCommandAlias("publishAll", ";valmuri/publish;cli/publish")
addCommandAlias("cleanAll", ";clean;valmuri/clean;cli/clean;helloWorld/clean")
