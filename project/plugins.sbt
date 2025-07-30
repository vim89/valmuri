// project/plugins.sbt - Valmuri Framework SBT Plugins

// Assembly plugin for creating executable CLI JAR
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "2.1.5")

// Code formatting and linting
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.2")
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.14.3")

// Publishing and release
addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.9.3")

// Development workflow improvements
addSbtPlugin("io.spray" % "sbt-revolver" % "0.10.0")

// Dependency management and updates
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.6.4")
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.10.0-RC1")

// Coverage reporting (optional)
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.0.9")

// Native packaging (for distribution)
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.16")
