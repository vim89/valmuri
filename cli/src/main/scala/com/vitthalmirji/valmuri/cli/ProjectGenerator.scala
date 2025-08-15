package com.vitthalmirji.valmuri.cli

import java.nio.file.{ Files, Paths }

object ProjectGenerator {

  def generateProject(name: String, template: String = "blog"): Unit = {
    val projectDir = Paths.get(name)

    // Create directory structure
    Files.createDirectories(projectDir.resolve("src/main/scala"))
    Files.createDirectories(projectDir.resolve("src/main/resources/static/css"))
    Files.createDirectories(projectDir.resolve("src/main/resources/static/js"))
    Files.createDirectories(projectDir.resolve("src/main/resources/templates"))
    Files.createDirectories(projectDir.resolve("src/main/resources/posts"))

    template match {
      case "blog" => generateBlogProject(projectDir, name)
      // case "api" => generateApiProject(projectDir, name)
      // case "portfolio" => generatePortfolioProject(projectDir, name)
      // case _ => generateBasicProject(projectDir, name)
    }

    println(s"✅ Created $name project with $template template")
    println(s"📁 cd $name")
    println(s"🚀 sbt run")
  }

  private def generateBlogProject(projectDir: java.nio.file.Path, name: String): Unit = {
    // Generate main application file
    val appContent = s"""
import valmuri.core.VApplication
import valmuri.http.VRoute
import valmuri.blog.VBlog

object ${name.capitalize}App extends VApplication {

  def routes() = List(
    VRoute.get("/", _ =>
      '''<!DOCTYPE html>
      <html><head><title>$name</title></head>
      <body>
        <h1>Welcome to $name</h1>
        <nav><a href="/blog">Blog</a></nav>
      </body></html>'''),

  ) ++ VBlog.generateRoutes()

  def main(args: Array[String]): Unit = {
    start()
    Thread.currentThread().join()
  }
}
    """.trim

    Files.write(projectDir.resolve("src/main/scala/Main.scala"), appContent.getBytes)

    // Generate sample blog post
    val postContent = """
# Welcome to My Blog

This is my first blog post using **Valmuri framework**.

## Features

- Simple markdown support
- Auto-generated routes
- Fast deployment

*Happy blogging!*
    """.trim

    Files.write(projectDir.resolve("src/main/resources/posts/welcome.md"), postContent.getBytes)

    // Generate build.sbt
    generateBuildFile(projectDir, name)

    // Generate Dockerfile
    generateDockerfile(projectDir, name)

    // Generate deployment script
    generateDeployScript(projectDir, name)
  }

  private def generateBuildFile(projectDir: java.nio.file.Path, name: String): Unit = {
    val buildContent = s"""
name := "$name"
version := "0.1.0"
scalaVersion := "2.13.12"

libraryDependencies += "io.valmuri" %% "valmuri" % "0.1.0"

mainClass := Some("${name.capitalize}App")
    """.trim

    Files.write(projectDir.resolve("build.sbt"), buildContent.getBytes)
  }

  private def generateDockerfile(projectDir: java.nio.file.Path, name: String): Unit = {
    val dockerContent = s"""
FROM openjdk:11-jre-slim

COPY target/scala-2.13/$name.jar /app.jar

EXPOSE 8080

CMD ["java", "-jar", "/app.jar"]
    """.trim

    Files.write(projectDir.resolve("Dockerfile"), dockerContent.getBytes)
  }

  private def generateDeployScript(projectDir: java.nio.file.Path, name: String): Unit = {
    val deployContent = s"""#!/bin/bash

# Build the application
echo "🔨 Building $name..."
sbt assembly

# Build Docker image
echo "🐳 Building Docker image..."
docker build -t $name .

# Deploy to Heroku (if Heroku CLI is installed)
if command -v heroku &> /dev/null; then
    echo "🚀 Deploying to Heroku..."
    heroku container:push web -a $name
    heroku container:release web -a $name
    echo "✅ Deployed to https://$name.herokuapp.com"
else
    echo "📦 Docker image built: $name"
    echo "🚀 Run locally: docker run -p 8080:8080 $name"
fi
    """.trim

    Files.write(projectDir.resolve("deploy.sh"), deployContent.getBytes)

    // Make script executable
    val deployScript = projectDir.resolve("deploy.sh").toFile
    deployScript.setExecutable(true)
  }
}
