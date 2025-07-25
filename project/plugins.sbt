// =============================================================================
// Flow Framework - sbt Plugins Configuration
// project/plugins.sbt
// =============================================================================

// Scala Native - for fast startup and single binary deployment
addSbtPlugin("org.scala-native" % "sbt-scala-native" % "0.4.17")

// Assembly - for creating executable CLI tool
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "2.1.5")

// Code formatting and linting
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.2")
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.11.1")

// Publishing and release
addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.5.12")

// Native packaging (for distribution)
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.16")

// Dependency management and updates
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.6.4")
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.10.0-RC1")

// Development workflow improvements
addSbtPlugin("io.spray" % "sbt-revolver" % "0.10.0")

// Coverage reporting (optional)
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.0.9")
