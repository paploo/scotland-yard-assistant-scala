package net.paploo.scotlandyard

import net.paploo.scotlandyard.board.Route.TransitMode.{Taxi, Bus}
import net.paploo.scotlandyard.board.{Board, Route, Station}
import net.paploo.scotlandyard.board.Board._
import net.paploo.scotlandyard.graph._

object SYApp {

  def main(args: Array[String]) {
    val nodes: Seq[Node[Station, Route]] = List(1,2,3).map( Station(_).toNode )

    val edges: Seq[Edge[Station, Route]] = List(
      Route(1, 2, Taxi),
      Route(2, 1, Taxi),
      Route(2, 1, Taxi),
      Route(1, 3, Bus),
      Route(3, 1, Bus),
      Route(2, 3, Taxi),
      Route(3, 2, Taxi)
    ).map(_.toEdge)

    implicit val graph: Graph[Station, Route] = Board(nodes, edges)

    val paths = Board.startingPaths(List(2))

    val p3 = paths.transitionEdges(_.data.transitMode == Taxi).transitionEdges(_.data.transitMode == Bus).filterNodes(_.data.num != 1).transitionEdges(_.data.transitMode == Taxi)
    println(p3)
    println(p3.headNodeOption.map(_.get.id))

    val p4 = paths.moveVia(Taxi).moveVia(Bus).detectiveAt(1).moveVia(Taxi)
    println(p4)
    println(p4.headNodeOption.map(_.get.id))
  }

}
