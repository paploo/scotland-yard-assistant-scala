package net.paploo.scotlandyard.graph

case class NodeID(id: Int) {
  override def toString = s"ID$id"
}

class Node[+N, +E](val id: NodeID, val data: N) {
  def edgesOption[N1 >: N, E1 >: E](implicit graph: Graph[N1,E1]): Option[Seq[Edge[N1,E1]]] = graph.edgeIndex.get(id)
  override def toString = s"Node($id, $data)"
}

class Edge[+N, +E](val sourceID: NodeID, val destID: NodeID, val data: E) {
  def sourceOption[N1 >: N, E1 >: E](implicit graph: Graph[N1,E1]): Option[Node[N1, E1]] = graph.nodeIndex.get(sourceID)
  def destOption[N1 >: N, E1 >: E](implicit graph: Graph[N1,E1]): Option[Node[N1, E1]] = graph.nodeIndex.get(destID)
  override def toString = s"Edge($sourceID, $destID, $data)"
}

object Graph {
}

class Graph[+N, +E](val nodes: Seq[Node[N,E]], val edges: Seq[Edge[N,E]]) {

  implicit val impliedGraph: Graph[N,E] = this

  val nodeIndex: Map[NodeID, Node[N,E]] = nodes.map(n => (n.id, n)).toMap

  val edgeIndex: Map[NodeID, Seq[Edge[N,E]]] = edges.groupBy(_.sourceID)

}