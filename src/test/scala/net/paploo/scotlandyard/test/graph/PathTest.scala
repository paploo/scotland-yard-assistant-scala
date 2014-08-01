package net.paploo.scotlandyard.test.graph

import net.paploo.scotlandyard.test.SpecTest
import net.paploo.scotlandyard.graph._

class PathTest extends SpecTest {

  val nodes: List[Node[Int, String]] = List(1,2,3,99).map(i => new Node(NodeID(i), i*100))

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

  describe("Path") {

    describe("filter") {

      it("should filter itself by the head node predicate") {
        val path1: Path[Int, String] = Path(List(NodeID(1)))
        val path2: Path[Int, String] = Path(List(NodeID(2), NodeID(1)))
        path1.filter(_.data == 200) should ===(None)
        path2.filter(_.data == 200) should ===(Some(path2))
      }

      it("should filter empty paths as None") {
        val path: Path[Int, String] = Path(Nil)
        path.filter(_ => true) shouldBe None
      }

      it("should filter paths with invalid node IDs as None") {
        val path: Path[Int, String] = Path(List(NodeID(-1)))
        path.filter(_ => true) shouldBe None
      }

    }

    describe("transition") {

      it("should transition on edges that match a predicate") {
        val path: Path[Int, String] = Path(List(NodeID(2)))
        val resultPaths: List[Path[Int, String]] = List(
          Path(List(NodeID(1), NodeID(2))),
          Path(List(NodeID(3), NodeID(2)))
        )
        path.transition(_.data == "Taxi") should ===(resultPaths)
      }

      it("should filter translitionless paths out") {
        val path: Path[Int, String] = Path(List(NodeID(99)))
        path.transition(_ => true) shouldBe Nil
      }

      it("should filter paths with illegal nodes out") {
        val path: Path[Int, String] = Path(List(NodeID(-1)))
        path.transition(_ => true) shouldBe Nil
      }

    }

    describe("headOption") {

      it("should return the head element of the path") {
        val path: Path[Int, String] = Path(List(NodeID(2), NodeID(1)))
        path.headOption.map(_.id) should ===(Some(NodeID(2)))
      }

      it("should return None if the path is empty") {
        val path: Path[Int, String] = Path(Nil)
        path.headOption shouldBe None
      }

      it("should return None if the head NodeID is invalid") {
        val path: Path[Int, String] = Path(List(NodeID(-1)))
        path.headOption shouldBe None
      }

    }

  }

  describe("PathSeq") {

    val paths: Seq[Path[Int, String]] = Seq(
      Path(List(NodeID(2), NodeID(1))),
      Path(List(NodeID(1))),
      Path(List(NodeID(99))),
      Path(List(NodeID(-1))),
      Path(Nil)
    )

    it("should filter") {
      val filtered = paths.filterNodes(node => (node.data / 100) % 2 == 1)
      val expected: Seq[Path[Int,String]] = Seq(
        Path(List(NodeID(1))),
        Path(List(NodeID(99)))
      )
      filtered should === (expected)
    }

    it("should transition") {
      val transitioned = paths.transitionEdges(edge => edge.data == "Taxi")
      val expected: Seq[Path[Int,String]] = Seq(
        Path(List(NodeID(1), NodeID(2), NodeID(1))),
        Path(List(NodeID(3), NodeID(2), NodeID(1))),
        Path(List(NodeID(2), NodeID(1)))
      )
      transitioned should === (expected)
    }

    it("should give the list of heads") {
      val expected: Seq[Option[NodeID]] = Seq(Some(NodeID(2)), Some(NodeID(1)), Some(NodeID(99)), None, None)
      paths.headNodeOptions.map(nodeOption => nodeOption.map(_.id)) should === (expected)
    }

    it("should compose") {
      val composedPath = paths.filterNodes(n => List(100, 200, 300).contains(n.data)).transitionEdges(_.data == "Taxi").filterNodes(_.data != 300)
      val expected: Seq[Path[Int,String]] = Seq(
        Path(List(NodeID(1), NodeID(2), NodeID(1))),
        Path(List(NodeID(2), NodeID(1)))
      )
      composedPath should === (expected)
    }

  }

}
