package net.paploo.scotlandyard.test.board

import net.paploo.scotlandyard.board.Route.TransitMode.{Taxi, Bus}
import net.paploo.scotlandyard.graph.{Path, NodeID}
import net.paploo.scotlandyard.test.SpecTest
import net.paploo.scotlandyard.board._
import net.paploo.scotlandyard.board.Board._

class BoardTest extends SpecTest {

  describe("Station") {

    it("should convert to node") {
      val station = new Station(88)
      val node = station.toNode

      node.id should === (NodeID(88))
      node.data shouldBe station
    }

  }

  describe("Route") {

    it("should convert to an edge") {
      val route = Route(1,3, Bus)
      val edge = route.toEdge

      edge.sourceID should === (NodeID(1))
      edge.destID should === (NodeID(3))
      edge.data shouldBe route
    }

  }

  describe("Board") {

    val stations: Seq[Station] = List(1,2,3).map(Station)

    val routes: Seq[Route] = List(
      Route(1, 2, Taxi),
      Route(2, 1, Taxi),
      Route(1, 3, Bus),
      Route(3, 1, Bus),
      Route(2, 3, Taxi),
      Route(3, 2, Taxi)
    )

    val startingNums = List(1,2)

    val board: Board = new Board(stations, routes, startingNums)

    describe("Path Implicit Class") {

      implicit val graph = board.graph
      val paths = board.startingPaths

      it("should filter out detectives") {
        val expected: Seq[Path[Station, Route]] = Seq(
          Path(List(NodeID(2)))
        )
        paths.detectiveAt(1) should === (expected)
      }

      it("should filter in MrX") {
        val expected: Seq[Path[Station, Route]] = Seq(
          Path(List(NodeID(1)))
        )
        paths.mrXAt(1) should === (expected)
      }

      it("should transition") {
        val expected: Seq[Path[Station, Route]] = Seq(
          Path(List(NodeID(3), NodeID(1)))
        )
        paths.moveVia(Bus) should === (expected)
      }

      it("should compose") {
        val expected: Seq[Path[Station, Route]] = Seq(
          Path(List(NodeID(3), NodeID(1), NodeID(2)))
        )

        val composedPaths = paths.moveVia(Taxi).detectiveAt(3).moveVia(Bus)

        composedPaths should === (expected)
      }

    }

    describe("starting paths") {

      it("should give the paths") {
        board.startingPaths should === (List(Path(List(NodeID(1))), Path(List(NodeID(2)))))
      }

      it("should give the node IDs") {
        board.startingNodeIDs should === (List(NodeID(1), NodeID(2)))
      }

    }

  }

}
