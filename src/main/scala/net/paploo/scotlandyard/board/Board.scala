package net.paploo.scotlandyard.board

import net.paploo.scotlandyard.graph.{Graph, Node, Edge, NodeID}

sealed trait Route {
  def toEdge(sourceNum: Int, destinationNum: Int): Edge[Station, Route] = new Edge(NodeID(sourceNum), NodeID(destinationNum), this)
}

object Route {
  case object Taxi extends Route
  case object Bus extends Route
  case object Underground extends Route
  case object Ferry extends Route
}

case class Station(num: Int) {
  def toNode: Node[Station, Route] = new Node(NodeID(num), this)
}

object Board {
  def apply[N <: Station, E <: Route](nodes: Seq[Node[N,E]], edges: Seq[Edge[N,E]]): Graph[N,E] = new Graph(nodes, edges)
}
