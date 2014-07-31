package net.paploo.scotlandyard

import net.paploo.scotlandyard.board.Route.TransitMode.{Taxi, Bus}
import net.paploo.scotlandyard.board.{Board, Route, Station}
import net.paploo.scotlandyard.board.Board._
import net.paploo.scotlandyard.graph._

object SYApp {

  def main(args: Array[String]) {
    val stations: Seq[Station] = List(1,2,3).map(Station)

    val routes: Seq[Route] = List(
      Route(1, 2, Taxi),
      Route(2, 1, Taxi),
      Route(2, 1, Taxi),
      Route(1, 3, Bus),
      Route(3, 1, Bus),
      Route(2, 3, Taxi),
      Route(3, 2, Taxi)
    )

    val board = new Board(stations, routes, List(2))

    val paths = board.startingPaths
    implicit val graph = board.graph

    //val p3 = paths.transitionEdges(_.data.transitMode == Taxi).transitionEdges(_.data.transitMode == Bus).filterNodes(_.data.num != 1).transitionEdges(_.data.transitMode == Taxi)
    //println(p3)
    //println(p3.headNodeOption.map(_.get.id))

    val p4 = paths.moveVia(Taxi).moveVia(Bus).detectiveAt(1).moveVia(Taxi)
    println(p4)
    println(p4.headNodeOption.map(_.get.id))
  }

}
