package net.paploo.scotlandyard.board

import net.paploo.scotlandyard.board.Route.TransitMode
import net.paploo.scotlandyard.graph._

object Route {

  sealed trait TransitMode {
    def name: String
    def value: Int
  }

  object TransitMode {
    def apply(name: String): TransitMode = Map(
      "Taxi" -> Taxi,
      "Bus" -> Bus,
      "Underground" -> Underground,
      "Ferry" -> Ferry
    ).getOrElse(name, Unknown(name))

    case object Taxi extends TransitMode { val name = "Taxi"; val value = 1 }
    case object Bus extends TransitMode { val name = "Bus"; val value = 2 }
    case object Underground extends TransitMode { val name = "Underground"; val value = 4 }
    case object Ferry extends TransitMode { val name = "Ferry"; val value = 8 }
    case class Unknown(name: String) extends TransitMode { val value = 99 }
  }

  implicit def ordering: Ordering[Route] = Ordering.by(r => (r.sourceNum, r.destinationNum, r.transitMode.value))

}

case class Route(sourceNum: Int, destinationNum: Int, transitMode: Route.TransitMode) {
  def toEdge: Edge[Station, Route] = new Edge(NodeID(sourceNum), NodeID(destinationNum), this)
}

case object Station {

  implicit def ordering: Ordering[Station] = Ordering.by(s => s.num)

}

case class Station(num: Int) {
  def toNode: Node[Station, Route] = new Node(NodeID(num), this)
}

object Board {

  object Graph {
    def apply(stations: Seq[Station], routes: Seq[Route]): Graph[Station, Route] = new Graph(stations.map(_.toNode), routes.map(_.toEdge))
  }

  implicit class BoardPath(val paths: Seq[Path[Station, Route]]) {

    def moveVia(transitMode: TransitMode)(implicit graph: Graph[Station, Route]): Seq[Path[Station, Route]] = paths.transitionEdges(_.data.transitMode == transitMode)

    def detectiveAt(stationNum: Int)(implicit graph: Graph[Station, Route]): Seq[Path[Station, Route]] = paths.filterNodes(_.data.num != stationNum)

    def mrXAt(stationNum: Int)(implicit graph: Graph[Station, Route]): Seq[Path[Station, Route]] = paths.filterNodes(_.data.num == stationNum)

  }

}

class Board(val stations: Seq[Station], val routes: Seq[Route], val startingStations: Seq[Station]) {

  val graph: Graph[Station, Route] = Board.Graph(stations, routes)

  val startingNodeIDs: Seq[NodeID] = startingStations.map(_.toNode.id)

  val startingPaths: Seq[Path[Station,Route]] = startingNodeIDs.map(id => new Path(List(id)))

}
