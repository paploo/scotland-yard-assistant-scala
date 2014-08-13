package net.paploo.scotlandyard

import net.paploo.scotlandyard.board.{Station, Route, Board}
import net.paploo.scotlandyard.graph.Path
import net.paploo.scotlandyard.cli.command.Command

import scala.annotation.tailrec
import scala.io.StdIn
import scala.util.{Try, Success, Failure}

object SYApp {

  case class GameState(board: Board, paths: Path[Station, Route])

  var games: Map[String, GameState] = Map()

  var currentGames: Option[String] = None

  def main(args: Array[String]): Unit = {
    mainLoop()
  }

  @tailrec
  protected def mainLoop(): Unit = {
    Try(Command(StdIn.readLine())) match {
      case Success(cmd) => processCommand(cmd)
      case Failure(err) => processFailure(err)
    }
    mainLoop()
  }

  protected def processCommand(cmd: Command): Unit = {
    cmd match {
      case _ => None //TODO: PROCESS COMMANDS
    }
  }

  protected def processFailure(err: Throwable): Unit = {
    //TODO: Display error!
  }

}
