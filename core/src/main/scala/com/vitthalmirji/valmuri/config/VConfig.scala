package com.vitthalmirji.valmuri.config

import java.util.Properties
import scala.util.Try

case class VConfig(
                    serverPort: Int = 8080,
                    serverHost: String = "localhost",
                    databaseUrl: String = "jdbc:sqlite:./valmuri.db",
                    actuatorEnabled: Boolean = true,
                    appName: String = "Valmuri Application",
                    appVersion: String = "0.1.0",
                    custom: Map[String, String] = Map.empty
                  )

object VConfig {
  def load(args: Array[String] = Array.empty, profile: String = ""): VConfig = {
    println("📋 Loading Valmuri configuration...")

    val props = new Properties()

    // Load application.properties
    loadPropertiesFile("application.properties", props)

    // Load profile-specific properties
    val activeProfile = getActiveProfile(args, profile)
    if (activeProfile.nonEmpty) {
      loadPropertiesFile(s"application-$activeProfile.properties", props)
      println(s"🌍 Active profile: $activeProfile")
    }

    // Override with environment variables
    loadEnvironmentVariables(props)

    // Override with command line arguments
    loadCommandLineArgs(args, props)

    val config = buildConfig(props)
    println(s"✅ Configuration loaded: ${config.appName} on ${config.serverHost}:${config.serverPort}")
    config
  }

  private def getActiveProfile(args: Array[String], defaultProfile: String): String = {
    args.sliding(2).collectFirst {
      case Array("--profile", profile) => profile
    }.orElse {
      Option(System.getenv("VALMURI_PROFILE"))
    }.getOrElse(defaultProfile)
  }

  private def loadPropertiesFile(filename: String, props: Properties): Unit = {
    Option(getClass.getClassLoader.getResourceAsStream(filename)) match {
      case Some(stream) =>
        props.load(stream)
        stream.close()
        println(s"📄 Loaded: $filename")
      case None => // File not found, skip
    }
  }

  private def loadEnvironmentVariables(props: Properties): Unit = {
    val envMappings = Map(
      "VALMURI_PORT" -> "server.port",
      "VALMURI_HOST" -> "server.host",
      "DATABASE_URL" -> "database.url"
    )

    envMappings.foreach { case (envVar, propKey) =>
      Option(System.getenv(envVar)) match {
        case Some(value) =>
          props.setProperty(propKey, value)
          println(s"🌐 Environment override: $envVar")
        case None => // No override
      }
    }
  }

  private def loadCommandLineArgs(args: Array[String], props: Properties): Unit = {
    args.sliding(2).foreach {
      case Array(key, value) if key.startsWith("--") =>
        props.setProperty(key.substring(2), value)
        println(s"⚡ Command line: $key = $value")
      case _ => // Skip invalid args
    }
  }

  private def buildConfig(props: Properties): VConfig = {
    def getProperty(key: String, default: String): String = props.getProperty(key, default)

    def getIntProperty(key: String, default: Int): Int =
      Try(props.getProperty(key, default.toString).toInt).getOrElse(default)

    def getBooleanProperty(key: String, default: Boolean): Boolean =
      Try(props.getProperty(key, default.toString).toBoolean).getOrElse(default)

    VConfig(
      serverPort = getIntProperty("server.port", 8080),
      serverHost = getProperty("server.host", "localhost"),
      databaseUrl = getProperty("database.url", "jdbc:sqlite:./valmuri.db"),
      actuatorEnabled = getBooleanProperty("actuator.enabled", default = true),
      appName = getProperty("app.name", "Valmuri Application"),
      appVersion = getProperty("app.version", "0.1.0")
    )
  }
}
