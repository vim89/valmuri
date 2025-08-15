package com.vitthalmirji.valmuri.config

import java.util.Properties
import java.io.{ File, FileInputStream }
import scala.jdk.CollectionConverters.DictionaryHasAsScala
import scala.util.{ Try, Using }

/**
 * Application configuration
 */
case class VConfig(
  // Application
  appName: String = "Valmuri Application",
  appVersion: String = "0.1.0",
  profile: String = "development",

  // Server
  serverHost: String = "localhost",
  serverPort: Int = 8080,
  serverThreads: Int = Runtime.getRuntime.availableProcessors() * 2,
  serverBacklog: Int = 100,
  serverShutdownDelay: Int = 5,
  maxRequestSize: Int = 10 * 1024 * 1024, // 10MB

  // Features
  actuatorEnabled: Boolean = true,
  corsEnabled: Boolean = true,
  corsOrigin: String = "*",

  // Paths
  staticDir: Option[String] = None,
  templateDir: Option[String] = None,
  uploadDir: Option[String] = Some("uploads"),

  // Database (future)
  databaseUrl: Option[String] = None,
  databaseDriver: Option[String] = None,
  databaseUsername: Option[String] = None,
  databasePassword: Option[String] = None,

  // Custom properties
  custom: Map[String, String] = Map.empty
)

object VConfig {

  def load(): VConfig = {
    val profile = sys.env.getOrElse("VALMURI_ENV", sys.props.getOrElse("valmuri.env", "development"))

    println(s"📋 Loading configuration for profile: $profile")

    // Load properties in order of precedence
    val props = new Properties()

    // 1. Load default properties
    loadResource("application.properties", props)

    // 2. Load profile-specific properties
    loadResource(s"application-$profile.properties", props)

    // 3. Load external config file if exists
    loadFile("application.properties", props)
    loadFile(s"application-$profile.properties", props)

    // 4. Override with environment variables
    loadEnvironment(props)

    // 5. Override with system properties
    loadSystemProperties(props)

    // Build config
    buildConfig(props, profile)
  }

  private def loadResource(name: String, props: Properties): Unit =
    Option(getClass.getClassLoader.getResourceAsStream(name)).foreach { stream =>
      Using.resource(stream) { s =>
        props.load(s)
        println(s"  ✓ Loaded resource: $name")
      }
    }

  private def loadFile(name: String, props: Properties): Unit = {
    val file = new File(name)
    if (file.exists()) {
      Using.resource(new FileInputStream(file)) { stream =>
        props.load(stream)
        println(s"  ✓ Loaded file: $name")
      }
    }
  }

  private def loadEnvironment(props: Properties): Unit = {
    val envMappings = Map(
      "VALMURI_HOST" -> "server.host",
      "VALMURI_PORT" -> "server.port",
      "PORT"         -> "server.port", // Heroku compatibility
      "DATABASE_URL" -> "database.url"
    )

    envMappings.foreach { case (env, prop) =>
      sys.env.get(env).foreach { value =>
        props.setProperty(prop, value)
        println(s"  ✓ Environment override: $env")
      }
    }
  }

  private def loadSystemProperties(props: Properties): Unit =
    sys.props.foreach { case (key, value) =>
      if (key.startsWith("valmuri.")) {
        val propKey = key.substring(8).replace("-", ".")
        props.setProperty(propKey, value)
        println(s"  ✓ System property: $key")
      }
    }

  private def buildConfig(props: Properties, profile: String): VConfig = {
    def get(key: String, default: String): String =
      props.getProperty(key, default)

    def getInt(key: String, default: Int): Int =
      Try(props.getProperty(key, default.toString).toInt).getOrElse(default)

    def getBoolean(key: String, default: Boolean): Boolean =
      Try(props.getProperty(key, default.toString).toBoolean).getOrElse(default)

    def getOption(key: String): Option[String] =
      Option(props.getProperty(key)).filter(_.nonEmpty)

    VConfig(
      appName = get("app.name", "Valmuri Application"),
      appVersion = get("app.version", "0.1.0"),
      profile = profile,
      serverHost = get("server.host", "localhost"),
      serverPort = getInt("server.port", 8080),
      serverThreads = getInt("server.threads", Runtime.getRuntime.availableProcessors() * 2),
      serverBacklog = getInt("server.backlog", 100),
      serverShutdownDelay = getInt("server.shutdown.delay", 5),
      maxRequestSize = getInt("server.max.request.size", 10 * 1024 * 1024),
      actuatorEnabled = getBoolean("actuator.enabled", true),
      corsEnabled = getBoolean("cors.enabled", true),
      corsOrigin = get("cors.origin", "*"),
      staticDir = getOption("static.dir"),
      templateDir = getOption("template.dir"),
      uploadDir = getOption("upload.dir"),
      databaseUrl = getOption("database.url"),
      databaseDriver = getOption("database.driver"),
      databaseUsername = getOption("database.username"),
      databasePassword = getOption("database.password"),
      custom = props.asScala
        .map { case (k, v) =>
          k.toString -> v.toString
        }
        .toMap
        .filter(_._1.startsWith("custom."))
    )
  }
}
