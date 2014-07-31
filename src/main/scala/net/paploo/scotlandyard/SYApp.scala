package net.paploo.scotlandyard

import net.paploo.scotlandyard.board.Route.{Taxi, Bus}
import net.paploo.scotlandyard.board.{Board, Route, Station}
import net.paploo.scotlandyard.graph._

object SYApp {

  def main(args: Array[String]) {
    val nodes: Seq[Node[Station, Route]] = List(1,2,3).map( Station(_).toNode )

    val edges: Seq[Edge[Station, Route]] = List(
      Taxi.toEdge(1, 2),
      Taxi.toEdge(2, 1),
      Bus.toEdge(1, 3),
      Bus.toEdge(3, 1),
      Taxi.toEdge(2, 3),
      Taxi.toEdge(3, 2)
    )

    implicit val graph = Board(nodes, edges)

    val path: Path[Station,Route] = Path(Seq(NodeID(2)))

    //val p2 = path.transition(_.data == Route("Taxi"))
    //println(p2.map(_.headOption.map(_.id).get))

    val p3 = Seq(path).transitionEdges(_.data == Taxi).transitionEdges(_.data == Bus).filterNodes(_.data.num != 1).transitionEdges(_.data == Taxi)
    println(p3)
    println(p3.headNodeOption.map(_.get.id))
  }

}
