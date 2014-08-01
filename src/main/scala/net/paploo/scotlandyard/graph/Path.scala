package net.paploo.scotlandyard.graph

object Path {

  def empty[N,E] = new Path(Nil)

  def apply[N,E](ids: Seq[NodeID]) = new Path(ids)

  def unapply[N,E](path: Path[N,E]): Seq[NodeID] = path.nodeIDs

  implicit class PathSeq[N, E](val paths: Seq[Path[N,E]]) {
    // Note: We have to be careful not to trample the normal sequence methods for implicit conversion.

    def transitionEdges[N1 >: N, E1 >: E](pred: Edge[N1, E1] => Boolean)(implicit graph: Graph[N1, E1]): Seq[Path[N1, E1]] =
      paths.flatMap(_.transition(pred))

    def filterNodes[N1 >: N, E1 >: E](pred: Node[N1, E1] => Boolean)(implicit graph: Graph[N1, E1]): Seq[Path[N1, E1]] =
      paths.map(_.filter(pred)).filter(_.isDefined).map(_.get)

    def headNodeIDs[N1 >: N, E1 >: E](implicit graph: Graph[N1, E1]): Seq[NodeID] = paths.map(_.nodeIDs.head)

    def headNodeOptions[N1 >: N, E1 >: E](implicit graph: Graph[N1, E1]): Seq[Option[Node[N1,E1]]] = paths.map(_.headOption(graph))
  }
}

class Path[+N, +E](ids: Seq[NodeID]) {

  val nodeIDs: List[NodeID] = ids.toList

  def filter[N1 >: N, E1 >: E](pred: Node[N1,E1] => Boolean)(implicit graph: Graph[N1,E1]): Option[Path[N1,E1]] =
    headOption(graph).filter(pred).map(_ => this)

  def transition[N1 >: N, E1 >: E](pred: Edge[N1, E1] => Boolean)(implicit graph: Graph[N1,E1]): Seq[Path[N1,E1]] =
    headOption(graph).flatMap(_.edgesOption).map(_.filter(pred)).getOrElse(Nil).map(_.destID :: nodeIDs).map( Path(_) )

  def headOption[N1 >: N, E1 >: E](implicit graph: Graph[N1,E1]): Option[Node[N1,E1]] = nodeIDs.headOption.flatMap(graph.nodeIndex.get)

  override def hashCode: Int = nodeIDs.hashCode()

  override def equals(that: Any): Boolean = that match {
    case p: Path[_, _] => nodeIDs == p.nodeIDs
    case _ => false
  }

  override def toString = s"Path($nodeIDs)"

}
