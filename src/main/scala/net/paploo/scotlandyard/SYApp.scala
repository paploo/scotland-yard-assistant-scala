package net.paploo.scotlandyard

import net.paploo.scotlandyard.graph._

object SYApp {

  case class Station(num: Int)
  case class Route(mode: String)

  def main(args: Array[String]) {
    val nodes: Seq[Node[Station, Route]] = List(
      new Node(NodeID(1), Station(1)),
      new Node(NodeID(2), Station(2)),
      new Node(NodeID(3), Station(3))
    )

    val edges: Seq[Edge[Station, Route]] = List(
      new Edge(NodeID(1), NodeID(2), Route("Taxi")),
      new Edge(NodeID(2), NodeID(1), Route("Taxi")),
      new Edge(NodeID(1), NodeID(3), Route("Bus")),
      new Edge(NodeID(3), NodeID(1), Route("Bus")),
      new Edge(NodeID(2), NodeID(3), Route("Taxi")),
      new Edge(NodeID(3), NodeID(2), Route("Taxi"))
    )

    implicit val graph = new Graph[Station, Route](nodes, edges)

    println(nodes)
    println(edges)
    println(graph)

    val path: Path[Station,Route] = Path(Seq(NodeID(2)))
    val p2 = path.transition(_.data == Route("Taxi"))
    println(p2.map(_.headOption.map(_.id).get))

    val p3 = Path.transition(Seq(path))(_.data == Route("Taxi"))
  }

}
