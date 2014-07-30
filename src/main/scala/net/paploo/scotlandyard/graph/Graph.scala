package net.paploo.scotlandyard.graph

case class NodeID(id: String)

class Node[+N, +E](val id: NodeID, val data: N) {
  def edgesOption[N1 >: N, E1 >: E](implicit graph: Graph[N1,E1]): Option[Seq[Edge[N1,E1]]] = graph.edgeIndex.get(id)
  override def toString = s"Node($id, $data)"
}

class Edge[+N, +E](val sourceID: NodeID, val destID: NodeID, val data: E) {
  def sourceOption[N1 >: N, E1 >: E](implicit graph: Graph[N1,E1]): Option[Node[N1, E1]] = graph.nodeIndex.get(sourceID)
  def destOption[N1 >: N, E1 >: E](implicit graph: Graph[N1,E1]): Option[Node[N1, E1]] = graph.nodeIndex.get(destID)
  override def toString = s"Edge($sourceID, $destID, $data)"
}

object Path {

  def apply[N,E](ids: Seq[NodeID]) = new Path(ids)

  def transition[N, E](paths: Seq[Path[N,E]])(pred: Edge[N,E] => Boolean)(implicit graph: Graph[N,E]): Seq[Path[N,E]] =
    paths.flatMap(_.transition(pred))

  def prune[N,E](paths: Seq[Path[N,E]])(pred: Node[N,E] => Boolean)(implicit graph: Graph[N,E]): Seq[Path[N,E]] =
    paths.map(_.prune(pred)).filter(_.isDefined).map(_.get)

}

class Path[+N, +E](ids: Seq[NodeID]) {
  val nodeIDs: List[NodeID] = ids.toList

  def filter[N1 >: N, E1 >: E](pred: Node[N1,E1] => Boolean)(implicit graph: Graph[N1,E1]): Option[Path[N1,E1]] =
    headOption(graph).filter(pred).map(_ => this)

  def prune[N1 >: N, E1 >: E](pred: Node[N1,E1] => Boolean)(implicit graph: Graph[N1,E1]): Option[Path[N1,E1]] =
    filter[N1, E1](!pred(_))(graph)

  def transition[N1 >: N, E1 >: E](pred: Edge[N1, E1] => Boolean)(implicit graph: Graph[N1,E1]): Seq[Path[N1,E1]] =
    headOption(graph).flatMap(_.edgesOption).map(_.filter(pred)).getOrElse(Nil).map(_.destID :: nodeIDs).map( Path(_) )

  def headOption[N1 >: N, E1 >: E](implicit graph: Graph[N1,E1]): Option[Node[N1,E1]] = nodeIDs.headOption.flatMap(graph.nodeIndex.get)
}

object Graph {
}

class Graph[+N, +E](val nodes: Seq[Node[N,E]], val edges: Seq[Edge[N,E]]) {

  implicit val impliedGraph: Graph[N,E] = this

  val nodeIndex: Map[NodeID, Node[N,E]] = nodes.map(n => (n.id, n)).toMap

  val edgeIndex: Map[NodeID, Seq[Edge[N,E]]] = edges.groupBy(_.sourceID)

}