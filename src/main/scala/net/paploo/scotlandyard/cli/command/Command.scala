package net.paploo.scotlandyard.cli.command

import net.paploo.scotlandyard.board.Board.{Ticket, Edition}
import net.paploo.scotlandyard.board.Route.TransitMode

import scala.util.matching.Regex

object Command {

  class CommandParseException(message: String) extends RuntimeException(message)
  class CommandArityException(message: String) extends CommandParseException(message)
  class CommandUnknownException(message: String) extends CommandParseException(message)

  def apply(cmd: String): Command = {
    val tokens: List[String] = cmd.toLowerCase.split("""\s+""").toList
    tokens match {
      case "quit" :: args => Quit
      case "exit" :: args => Quit
      case "new" :: edition :: ref :: Nil if Edition.fromString.isDefinedAt(edition.toLowerCase) => NewGame(Edition.fromString(edition.toLowerCase), ref)
      case "new" :: args => throw new CommandArityException("new (miltonbradley|ravensburger) <id>")
      case "set" :: ref :: Nil => SetGame(ref)
      case "set" :: args => throw new CommandArityException("set <id>")
      case "list" :: Nil => ListGames
      case "list" :: "paths" :: Nil => GetPaths(None)
      case "list" :: "paths" :: ref :: Nil => GetPaths(Some(ref))
      case "list" :: args => throw new CommandArityException("list [paths [<ref>]]")
      case "move" :: "via" :: ticket :: Nil if Ticket.fromString.isDefinedAt(ticket.toLowerCase) => MoveVia(Ticket.fromString(ticket.toLowerCase))
      case "move" :: ticket :: Nil if Ticket.fromString.isDefinedAt(ticket.toLowerCase) => MoveVia(Ticket.fromString(ticket.toLowerCase))
      case "move" :: args => throw new CommandArityException("move [via] (taxi|bus|underround|blackticket)")
      case "detective" :: "at" :: stationNum :: Nil if stationNum matches """(\d)+""" => DetectiveAt(stationNum.toInt)
      case "detective" :: stationNum :: Nil if stationNum matches """(\d)+""" => DetectiveAt(stationNum.toInt)
      case "detective" :: args => throw new CommandArityException("detective [at] <stationNumber>")
      case "mrx" :: "at" :: stationNum :: Nil if stationNum matches """(\d)+""" => MrXAt(stationNum.toInt)
      case "mrx" :: stationNum :: Nil if stationNum matches """(\d)+""" => MrXAt(stationNum.toInt)
      case "mrx" :: args => throw new CommandArityException("mrx [at] <stationNumber>")
      case "mr" :: "x" :: "at" :: stationNum :: Nil if stationNum matches """(\d)+""" => MrXAt(stationNum.toInt)
      case "mr" :: "x" :: stationNum :: Nil if stationNum matches """(\d)+""" => MrXAt(stationNum.toInt)
      case "mr" :: "x" :: args => throw new CommandArityException("mr x [at] <stationNumber>")
      case c :: args => throw new CommandUnknownException(s"No command for $c")
    }
  }
}

sealed trait Command

case class NewGame(edition: Edition, ref: String) extends Command
case class SetGame(ref: String) extends Command
case object ListGames extends Command
case class GetPaths(ref: Option[String]) extends Command
case class MoveVia(ticket: Ticket) extends Command
case class DetectiveAt(stationNum: Int) extends Command
case class MrXAt(stationNum: Int) extends Command
case object Quit extends Command
