package net.paploo.scotlandyard.cli.command

import net.paploo.scotlandyard.board.Route.TransitMode

import scala.util.matching.Regex

object Command {

  def apply(cmd: String): Command = cmd.toLowerCase match {
    case newGameRegex(edition, ref) => NewGame(Edition(edition), ref)
    case setGameRegex(ref) => SetGame(ref)
    case c => InvalidCommand(c)
  }

  val newGameRegex: Regex = """new (miltonbradley|ravensburger) ([A-Za-z0-9]+)""".r
  val setGameRegex: Regex = """set ([A-Za-z0-9]+""".r
  val listGamesRegex: Regex = """list""".r
  val moveViaRegex: Regex = """move (taxi|bus|underground)""".r
  val moveViaBlackTicketRegex: Regex = """move blackticket""".r

  object Edition {
    def apply(str: String): Edition = str.toLowerCase match {
      case "miltonbradley" => MiltonBradley
      case "ravensburger" => Ravensburger
      case s => throw new IllegalArgumentException(s"No Edition for String $s")
    }
  }
  sealed trait Edition
  case object MiltonBradley extends Edition
  case object Ravensburger extends Edition

}

sealed trait Command

case class NewGame(edition: Command.Edition, ref: String) extends Command
case class SetGame(ref: String) extends Command
case object ListGames extends Command
case class MoveVia(transitMode: TransitMode) extends Command
case object MoveViaBlackTicket extends Command
//case class DetectiveAt(stationNum: Int) extends Command
//case class MrXAt(stationNum: Int) extends Command
case class InvalidCommand(cmd: String) extends Command
