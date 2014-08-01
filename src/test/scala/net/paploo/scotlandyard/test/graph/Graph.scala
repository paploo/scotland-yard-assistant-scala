package net.paploo.scotlandyard.test.graph

import net.paploo.scotlandyard.test.SpecTest
import net.paploo.scotlandyard.graph._

class GraphTest extends SpecTest {

  val nodes: List[Node[Int, String]] = List(1,2,3,4).map(i => new Node(NodeID(i), i*100))

  val edges: List[Edge[Int, String]] = List(
    (1 ,2, "Taxi"),
    (1, 3, "Bus"),
    (2, 1, "Taxi"),
    (2, 3, "Taxi"),
    (3, 1, "Bus"),
    (3, 2, "Taxi"),
    (55, 66, "Bad")
  ).map {case (src,dest,mode) => new Edge(NodeID(src), NodeID(dest), mode)}

  implicit val graph: Graph[Int, String] = new Graph(nodes, edges)

  describe("Node") {

    it("should provide the data") {
      nodes.head.data should === (100)
    }

    it("should resolve the edges radiating away as an option") {
      val node = nodes.head
      node.edgesOption.get.map(_.sourceID).toSet should === (Set(NodeID(1)))
    }

    it("should resolve to None if there are no edges") {
      val node = nodes.last
      node.edgesOption should === (None)
    }

  }

  describe("Edge") {

    it("should provide the data") {
      edges.head.data should === ("Taxi")
    }

    it("should give the source node") {
      edges.head.sourceOption.get.id should === (NodeID(1))
    }

    it("should give the destNode") {
      edges.head.destOption.get.id should === (NodeID(2))
    }

    it("should give None if the source node doesn't exist") {
      edges.last.sourceOption should === (None)
    }

    it("should give None if the dest node doesn't exist") {
      edges.last.sourceOption should === (None)
    }

  }

  describe("Graph") {

    it("should allow fetching a node by ID") {
      graph(NodeID(1)) should === (nodes.head)
    }

    it("should allow fetching a node option by ID") {
      graph.get(NodeID(1)) should === (Some(nodes.head))
    }

  }

}
