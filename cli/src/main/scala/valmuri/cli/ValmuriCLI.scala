package valmuri.cli

import scopt.OParser
import valmuri.cli.generators.ProjectScaffolder

object ValmuriCLI {

  /** Configuration for CLI parsing */
  case class CLIConfig(command: ValmuriCommand = ValmuriCommand.Help)

  def main(args: Array[String]): Unit = {
    val builder = OParser.builder[CLIConfig]
    val parser = {
      import builder._
      OParser.sequence(
        programName("valmuri"),
        head("Valmuri CLI", "0.1.0"),

        // Create new app
        cmd("new")
          .text("Create a new Valmuri app")
          .children(
            arg[String]("<name>")
              .required()
              .action((name, c) => c.copy(command = ValmuriCommand.NewApp(name)))
              .text("Name of the new app")
          ),

        // Start dev mode
        cmd("dev")
          .text("Start the development server")
          .action((_, c) => c.copy(command = ValmuriCommand.Dev)),

        // Version
        cmd("version")
          .text("Print CLI version")
          .action((_, c) => c.copy(command = ValmuriCommand.Version)),

        // Database commands
        cmd("db")
          .text("Database commands")
          .children(
            cmd("migrate")
              .text("Run database migrations")
              .action((_, c) => c.copy(command = ValmuriCommand.Migrate)),

            cmd("rollback")
              .text("Rollback last migration")
              .action((_, c) => c.copy(command = ValmuriCommand.Rollback))
          )
      )
    }

    OParser.parse(parser, args, CLIConfig()) match {
      case Some(CLIConfig(command)) => run(command)
      case None =>
        println("❌ Invalid command. Run `valmuri --help` for usage.")
    }
  }

  def run(command: ValmuriCommand): Unit = command match {
    case ValmuriCommand.NewApp(name) =>
      ProjectScaffolder.scaffold(name)

    case ValmuriCommand.Dev =>
      println("🚀 Dev mode coming soon...")

    case ValmuriCommand.Version =>
      println("Valmuri CLI version 0.1.0")

    case ValmuriCommand.Migrate =>
      println("📦 DB migration support coming soon...")

    case ValmuriCommand.Rollback =>
      println("↩️ DB rollback support coming soon...")

    case ValmuriCommand.Help =>
      println("ℹ️ Run `valmuri --help` to view all commands.")
  }
}
