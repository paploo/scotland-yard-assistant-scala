package net.paploo.scotlandyard

import net.paploo.scotlandyard.board.Route.TransitMode.{Taxi, Bus}
import net.paploo.scotlandyard.board.parser.Parser
import net.paploo.scotlandyard.board.{Board, Route, Station}
import net.paploo.scotlandyard.board.Board._
import net.paploo.scotlandyard.graph._

object SYApp {

  def main(args: Array[String]) {
    val stations: Seq[Station] = List(1,2,3).map(Station(_))

    val routes: Seq[Route] = List(
      Route(1, 2, Taxi),
      Route(2, 1, Taxi),
      Route(1, 3, Bus),
      Route(3, 1, Bus),
      Route(2, 3, Taxi),
      Route(3, 2, Taxi)
    )

    val board = new Board(stations, routes, List(Station(2)))

    val paths = board.startingPaths
    implicit val graph = board.graph

    val ps = paths.moveVia(Taxi).moveVia(Bus).detectiveAt(1).moveVia(Taxi)
    println(ps) // Ex: List(Path(List(ID2, ID3, ID1, ID2)))
    println(ps.headNodeOptions.map(_.get.id)) // Ex: List(ID2)

    //val parser = Parser.CSV("1\n2\n1,2,Taxi\n2,1,Bus")
    val parser = Parser.CSV("1\n2\n1,2,Taxi\n2,1,Bus\n1,2,Taxi\n3,3,Eeloo\na,b,c")
    println(parser.errors.toList)
    println(parser.board)
    println(parser.board.startingStations)
    println(parser.board.stations)
    println(parser.board.routes)
  }

}
