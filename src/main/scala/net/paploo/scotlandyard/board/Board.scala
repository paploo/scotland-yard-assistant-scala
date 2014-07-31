package net.paploo.scotlandyard.board

import java.io.File

import net.paploo.scotlandyard.board.Route.TransitMode
import net.paploo.scotlandyard.graph._

object Route {

  sealed trait TransitMode

  object TransitMode {
    case object Taxi extends TransitMode
    case object Bus extends TransitMode
    case object Underground extends TransitMode
    case object Ferry extends TransitMode
  }

}

case class Route(sourceNum: Int, destinationNum: Int, transitMode: Route.TransitMode) {
  def toEdge: Edge[Station, Route] = new Edge(NodeID(sourceNum), NodeID(destinationNum), this)
}


case class Station(num: Int) {
  def toNode: Node[Station, Route] = new Node(NodeID(num), this)
}

object Board {

  //def apply(tokenizedRows: Seq[Seq[String]])(startingStationNums: Seq[Int]): (Seq[Station], Seq[Route]) = ???

  //def apply(file: File)(codec: BoardCodec)(startingStationNums: Seq[Int]: Graph[Station, Route] = ???

  object Graph {
    def apply(stations: Seq[Station], routes: Seq[Route]): Graph[Station, Route] = new Graph(stations.map(_.toNode), routes.map(_.toEdge))
  }

  implicit class BoardPath(val paths: Seq[Path[Station, Route]]) {

    def moveVia(transitMode: TransitMode)(implicit graph: Graph[Station, Route]): Seq[Path[Station, Route]] = paths.transitionEdges(_.data.transitMode == transitMode)

    def detectiveAt(stationNum: Int)(implicit graph: Graph[Station, Route]): Seq[Path[Station, Route]] = paths.filterNodes(_.data.num != stationNum)

    def mrXAt(stationNum: Int)(implicit graph: Graph[Station, Route]): Seq[Path[Station, Route]] = paths.filterNodes(_.data.num == stationNum)

  }

}

class Board(val stations: Seq[Station], val routes: Seq[Route], val startingStationNums: Seq[Int]) {

  val graph: Graph[Station, Route] = Board.Graph(stations, routes)

  lazy val startingNodeIDs: Seq[NodeID] = startingStationNums.map(Station(_).toNode.id)

  lazy val startingPaths: Seq[Path[Station,Route]] = startingNodeIDs.map(id => new Path(List(id)))

}
