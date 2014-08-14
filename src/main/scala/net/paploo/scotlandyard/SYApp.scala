package net.paploo.scotlandyard

import net.paploo.scotlandyard.board.{Station, Route, Board}
import net.paploo.scotlandyard.graph.{Graph, Path}
import net.paploo.scotlandyard.board.Board.BoardPath
import net.paploo.scotlandyard.cli.command._

import scala.annotation.tailrec
import scala.io.StdIn
import scala.util.{Try, Success, Failure}

object SYApp {

  case class GameState(board: Board, paths: Seq[Path[Station, Route]])

  var games: Map[String, GameState] = Map()
  var currentGameKey: Option[String] = None
  def currentGame: Option[GameState] = currentGameKey.flatMap(k => games.get(k))

  def main(args: Array[String]): Unit = {
    mainLoop()
  }

  @tailrec
  protected def mainLoop(): Unit = {
    Console.print(prompt)
    Try(Command(StdIn.readLine())) match {
      case Success(cmd) => processCommand(cmd)
      case Failure(err) => processFailure(err)
    }
    mainLoop()
  }

  protected def prompt: String = {
    val fmtCurrGameKey = currentGameKey match {
      case Some(k) => s"${Console.BLUE}$k${Console.RESET}"
      case None => s"(${Console.YELLOW}no game${Console.RESET})"
    }
    s"$fmtCurrGameKey> "
  }

  protected def processCommand(cmd: Command): Unit = {
    //TODO: Match should return Option[String], and we log if it has a value.
    cmd match {
      case Quit => System.exit(0)
      case NewGame(edition, ref) =>
        val board = Board(edition).get
        val game = GameState(board, board.startingPaths)
        games = games + (ref -> game)
        currentGameKey = Some(ref)
      case SetGame(ref) =>
        currentGameKey = Some(ref)
      case ListGames =>
        Console.println(games.keys.mkString("  ", "\n  ", ""))
      case GetPaths(None) =>
        currentGame.map(game => Console.println(formatPaths(game.paths)(game.board.graph)))
      case GetPaths(Some(ref)) =>
        games.get(ref).map(game => Console.println(formatPaths(game.paths)(game.board.graph)))
      case MoveVia(ticket) =>
        currentGame.map(game => game.paths.moveVia(ticket)(game.board.graph)) //TODO: Need to actually write the board to the game state!
      case DetectiveAt(stationNum: Int) =>
        currentGame.map(game => game.paths.detectiveAt(stationNum)(game.board.graph)) //TODO: Need to actually write the board to the game state!
      case MrXAt(stationNum: Int) =>
        currentGame.map(game => game.paths.mrXAt(stationNum)(game.board.graph)) //TODO: Need to actually write the board to the game state
    }
    Console.println(s"=> ${Console.GREEN}Done: $cmd${Console.RESET}\n")
  }

  protected def processFailure(err: Throwable): Unit = {
    Console.println(s"=> ${Console.RED}ERROR: ${err.getMessage}${Console.RESET}")
  }

  protected def formatPaths(paths: Seq[Path[Station, Route]])(implicit graph: Graph[Station,Route]): String =
    paths.map(path => path.nodeIDs.map(id => graph(id).data.num).map(num => f"$num%3d").mkString(", ")).mkString("\n")

}
