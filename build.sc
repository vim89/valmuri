import mill._, scalalib._, scalanativelib._

// =============================================================================
// Flow Framework - True Integrated Framework (Not Library Collection)
// =============================================================================

// Core framework - users depend on this, not individual libraries
object valmuri extends ScalaModule {
  def scalaVersion = "2.13.12"

  // These are OUR implementation dependencies - users never import these directly
  def ivyDeps = Agg(
    // Effect system (our runtime foundation)
    ivy"dev.zio::zio::2.0.19",

    // JSON handling (internal - users use our JSON APIs)
    ivy"com.lihaoyi::upickle::3.1.4",

    // Database (internal - users use our ORM APIs)
    ivy"org.xerial:sqlite-jdbc:3.43.0.0",

    // Configuration (internal - users use our config APIs)
    ivy"com.lihaoyi::mainargs::0.6.2"
  )

  // Framework metadata
  def artifactName = "valmuri"
  def version = "0.1.0-SNAPSHOT"

  // Compile settings optimized for development speed
  def scalacOptions = Seq(
    "-deprecation",
    "-feature",
    "-unchecked",
    "-Xfatal-warnings",
    "-encoding", "UTF-8"
  )

  // Test configuration
  object test extends Tests with TestModule.Munit {
    def ivyDeps = Agg(
      ivy"org.scalameta::munit::1.0.0-M10"
    )
  }
}

// CLI tool for framework (valmuri new, valmuri dev, valmuri generate, etc.)
object valmuriCli extends ScalaModule {
  def scalaVersion = "2.13.12"
  def moduleDeps = Seq(valmuri)

  def ivyDeps = Agg(
    ivy"com.lihaoyi::mainargs::0.6.2"
  )

  def artifactName = "valmuri-cli"

  // This will be the `valmuri` command users run
  def assembly = T {
    super.assembly().path
  }
}

// Example application using ONLY Valmuri framework APIs
object valmuriExamples extends Module {
  object helloWorld extends ScalaModule {
    def scalaVersion = "2.13.12"
    def moduleDeps = Seq(valmuri)

    // Example has no direct dependencies on ZIO, upickle, etc.
    // It only depends on our framework
    def ivyDeps = Agg()

    def artifactName = "valmuri-examples"
  }
}

// Documentation module (optional)
object valmuriDocs extends ScalaModule {
  def scalaVersion = "2.13.12"
  def moduleDeps = Seq(valmuri)

  def ivyDeps = Agg(
    ivy"com.lihaoyi::scalatags::0.12.0"
  )
}

// =============================================================================
// Development Commands
// =============================================================================

// Build everything and run tests
def buildAll() = T.command {
  valmuri.compile()
  valmuri.test.test()
  valmuriCli.compile()
  valmuriExamples.helloWorld.compile()
}

// Run the hello world example
def runExample() = T.command {
  valmuriExamples.helloWorld.runBackground()
}

// Package the CLI tool for distribution
def packageCli() = T.command {
  valmuriCli.assembly()
}

// Run framework tests with coverage
def testFramework() = T.command {
  valmuri.test.test()
}

// Development server with auto-reload (future enhancement)
def dev() = T.command {
  println("🚀 Starting development server...")
  valmuriExamples.helloWorld.runBackground()
}
