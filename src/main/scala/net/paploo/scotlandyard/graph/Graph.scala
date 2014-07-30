package net.paploo.scotlandyard.graph

case class NodeID(id: String)

class Node[+N, +E](val id: NodeID, val data: N) {
  def edges[N1 >: N, E1 >: E](implicit graph: Graph[N1,E1]): Option[Seq[Edge[N1,E1]]] = graph.edgeIndex.get(id)
  override def toString = s"Node($id, $data)"
}

class Edge[+N, +E](val sourceID: NodeID, val destID: NodeID, val data: E) {
  def source[N1 >: N, E1 >: E](implicit graph: Graph[N1,E1]): Option[Node[N1, E1]] = graph.nodeIndex.get(sourceID)
  def dest[N1 >: N, E1 >: E](implicit graph: Graph[N1,E1]): Option[Node[N1, E1]] = graph.nodeIndex.get(destID)
  override def toString = s"Edge($sourceID, $destID, $data)"
}

object Path {

  def filter[N,E](nodeIDs: List[NodeID])(pred: Node[N,E] => Boolean)(implicit graph: Graph[N,E]): Option[List[NodeID]] =
    nodeIDs.headOption.flatMap(graph.nodeIndex.get).filter(pred).map(_ => nodeIDs)

  def prune[N,E](nodeIDs: List[NodeID])(pred: Node[N,E] => Boolean)(implicit graph: Graph[N,E]): Option[List[NodeID]] =
   filter(nodeIDs)((node: Node[N,E]) => !pred(node))

  def transition[N,E](nodeIDs: List[NodeID])(pred: Edge[N,E] => Boolean)(implicit graph: Graph[N,E]): Seq[List[NodeID]] =
    nodeIDs.headOption.flatMap(graph.edgeIndex.get).map(_.filter(pred)).getOrElse(Nil).map(_.destID :: nodeIDs)
}

class Path[+N, +E](ids: NodeID*)(gph: Graph[N,E]) {
  val nodeIDs: List[NodeID] = ids.toList

  implicit val impliedGraph: Graph[N,E] = gph

  def filter[N1 >: N, E1 >: E](pred: Node[N1,E1] => Boolean)(implicit graph: Graph[N1,E1]): Option[Path[N1,E1]] =
    nodeIDs.headOption.flatMap(graph.nodeIndex.get).filter(pred).map(_ => this)

  def prune[N1 >: N, E1 >: E](pred: Node[N1,E1] => Boolean)(implicit graph: Graph[N1,E1]): Option[Path[N1,E1]] =
    filter(!pred(_))

  def transition[N1 >: N, E1 >: E](pred: Edge[N1, E1] => Boolean)(implicit graph: Graph[N1,E1]): Seq[Path[N1,E1]] =
    nodeIDs.headOption.flatMap(graph.edgeIndex.get).map(_.filter(pred)).getOrElse(Nil).map(_.destID :: nodeIDs).map( new Path(_: _*)(graph) )
}

object Graph {
}

class Graph[+N, +E](val nodes: Seq[Node[N,E]], val edges: Seq[Edge[N,E]]) {

  implicit val impliedGraph: Graph[N,E] = this

  val nodeIndex: Map[NodeID, Node[N,E]] = nodes.map(n => (n.id, n)).toMap

  val edgeIndex: Map[NodeID, Seq[Edge[N,E]]] = edges.groupBy(_.sourceID)

  def transition[N1 >: N, E1 >: E](paths: Seq[Path[N1,E1]])(pred: Edge[N1,E1] => Boolean): Seq[Path[N1, E1]] =
    paths.flatMap(_.transition(pred))

  def prune[N1 >: N, E1 >: E](paths: Seq[Path[N1,E1]])(pred: Node[N1,E1] => Boolean): Seq[Path[N1, E1]] = ???
    //paths.map(path => prune(path)(pred)).filter(_.isDefined).map(_.get)
}
