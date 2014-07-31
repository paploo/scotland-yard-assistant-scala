package net.paploo.scotlandyard.board

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

  def apply[N <: Station, E <: Route](nodes: Seq[Node[N,E]], edges: Seq[Edge[N,E]]): Graph[N,E] = new Graph(nodes, edges)

  def startingNodeIDs(startingNums: Seq[Int]): Seq[NodeID] = startingNums.map(Station(_).toNode.id)

  def startingPaths(startingNums: Seq[Int]): Seq[Path[Station,Route]] = startingNodeIDs(startingNums).map(id => new Path(List(id)))

  implicit class BoardPath(val paths: Seq[Path[Station, Route]]) {

    def moveVia(transitMode: TransitMode)(implicit graph: Graph[Station, Route]): Seq[Path[Station, Route]] = paths.transitionEdges(_.data.transitMode == transitMode)

    def detectiveAt(stationNum: Int)(implicit graph: Graph[Station, Route]): Seq[Path[Station, Route]] = paths.filterNodes(_.data.num != stationNum)

    def mrXAt(stationNum: Int)(implicit graph: Graph[Station, Route]): Seq[Path[Station, Route]] = paths.filterNodes(_.data.num == stationNum)

  }

}
