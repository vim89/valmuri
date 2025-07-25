package valmuri.cli

/** All supported commands in the Valmuri CLI */
sealed trait ValmuriCommand

object ValmuriCommand {

  /** `valmuri new <name>` */
  case class NewApp(name: String) extends ValmuriCommand

  /** `valmuri dev` */
  case object Dev extends ValmuriCommand

  /** `valmuri version` */
  case object Version extends ValmuriCommand

  /** `valmuri db migrate` */
  case object Migrate extends ValmuriCommand

  /** `valmuri db rollback` */
  case object Rollback extends ValmuriCommand

  /** Default fallback or `--help` */
  case object Help extends ValmuriCommand
}
