package valmuri.cli.generators

import java.nio.file.{Files, Paths, StandardOpenOption}

object ProjectScaffolder {
  def scaffold(appName: String): Unit = {
    val base = Paths.get(appName)
    val src = base.resolve("src/main/scala/valmuri")
    val resources = base.resolve("src/main/resources")
    val test = base.resolve("src/test/scala/valmuri")
    val plugins = base.resolve("project")
    val mainFile = src.resolve("Main.scala")
    val sbtFile = base.resolve("build.sbt")
    val pluginsSbt = plugins.resolve("plugins.sbt")

    def write(path: java.nio.file.Path, content: String): Unit = {
      Files.createDirectories(path.getParent)
      Files.write(
        path,
        content.getBytes("UTF-8"),
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING
      )
    }

    write(sbtFile,
      s"""|ThisBuild / scalaVersion := "2.13.12"
          |
          |lazy val root = (project in file("."))
          |  .settings(
          |    name := "$appName",
          |    libraryDependencies ++= Seq(
          |      "com.vitthalmirji" %% "valmuri" % "0.1.0-SNAPSHOT"
          |    )
          |  )
          |""".stripMargin)

    write(pluginsSbt, "// Add your SBT plugins here\n")

    write(mainFile,
      """|package valmuri
         |
         |object Main extends App {
         |  println("🎉 Hello from your new Valmuri app!")
         |}
         |""".stripMargin)

    println(s"✅ Project '$appName' scaffolded at: ${base.toAbsolutePath}")
  }
}
