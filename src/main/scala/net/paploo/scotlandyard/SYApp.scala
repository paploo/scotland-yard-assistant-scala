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
  var currentGameRef: Option[String] = None
  def currentGame: Option[GameState] = currentGameRef.flatMap(k => games.get(k))

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
    val fmtCurrGameRef = currentGameRef match {
      case Some(k) => s"${Console.BLUE}$k${Console.RESET}"
      case None => s"(${Console.YELLOW}no game${Console.RESET})"
    }
    s"$fmtCurrGameRef> "
  }

  protected def processCommand(cmd: Command): Unit = {
    performCommand(cmd).map(Console.println(_))
    Console.println(s"=> ${Console.GREEN}Done: $cmd${Console.RESET}\n")
  }

  protected def processFailure(err: Throwable): Unit = {
    Console.println(s"=> ${Console.RED}ERROR: ${err.getMessage}${Console.RESET}")
  }

  protected def performCommand(cmd: Command): Option[String] = cmd match {
    case Quit =>
      System.exit(0)
      None
    case NewGame(edition, ref) =>
      val board = Board(edition).get
      val game = GameState(board, board.startingPaths)
      games = games + (ref -> game)
      currentGameRef = Some(ref)
      Some(s"Game created with id $ref.")
    case SetGame(ref) =>
      currentGameRef = Some(ref)
      Some(s"Set game to id $ref.")
    case ListGames =>
      Some(games.keys.mkString("  ", "\n  ", ""))
    case GetPaths(None) =>
      currentGame.map(game => formatPaths(game.paths)(game.board.graph))
    case GetPaths(Some(ref)) =>
      games.get(ref).map(game => formatPaths(game.paths)(game.board.graph))
    case MoveVia(ticket) =>
      currentGame.flatMap(game => updateCurrentPaths(game.paths.moveVia(ticket)(game.board.graph))).flatMap(_ => performCommand(GetPaths(None)))
    case DetectiveAt(stationNum: Int) =>
      currentGame.flatMap(game => updateCurrentPaths(game.paths.detectiveAt(stationNum)(game.board.graph))).flatMap(_ => performCommand(GetPaths(None)))
    case MrXAt(stationNum: Int) =>
      currentGame.flatMap(game => updateCurrentPaths(game.paths.mrXAt(stationNum)(game.board.graph))).flatMap(_ => performCommand(GetPaths(None)))
  }

  protected def formatPaths(paths: Seq[Path[Station, Route]])(implicit graph: Graph[Station,Route]): String =
    paths.map(path => path.nodeIDs.map(id => graph(id).data.num).map(num => f"$num%3d").mkString(", ")).mkString("\n")

  protected def updateCurrentPathsFunctional(newPaths: => Seq[Path[Station, Route]]): Option[GameState] = currentGameRef.flatMap { ref =>
    currentGame.map { game =>
      val newGame = game.copy(paths = newPaths)
      games = games + (ref -> newGame)
      newGame
    }
  }

  protected def updateCurrentPaths(pathComputer: Function[GameState, Path[Station, Route]]): Option[GameState] = for {
    ref <- currentGameRef
    game <- currentGame
    newPaths <- pathComputer(game)
    newGame = game.copy(paths = newPaths)
  } yield {
    games = games + (ref -> newGame)
    newGame
  }

  protected def updateCurrentPaths(newPaths: => Seq[Path[Station, Route]]): Option[GameState] = for {
    ref <- currentGameRef
    game <- currentGame
    newGame = game.copy(paths = newPaths)
  } yield {
    games = games + (ref -> newGame)
    newGame
  }

}
