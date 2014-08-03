package net.paploo.scotlandyard.test.board

import net.paploo.scotlandyard.board.Route.TransitMode.{Underground, Taxi, Bus}
import net.paploo.scotlandyard.graph.{Path, NodeID}
import net.paploo.scotlandyard.test.SpecTest
import net.paploo.scotlandyard.board._
import net.paploo.scotlandyard.board.Board._
//import net.paploo.scotlandyard.board.Route._
//import net.paploo.scotlandyard.board.Station._

import scala.collection.immutable.SortedSet

class BoardTest extends SpecTest {

  describe("Station") {

    it("should convert to node") {
      val station = new Station(88)
      val node = station.toNode

      node.id should === (NodeID(88))
      node.data shouldBe station
    }

    it("should work in an sorted set") {
      val set = SortedSet(Station(2), Station(1), Station(2))
      set.toList should === (List(Station(1), Station(2)))
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

    it("should work in a sorted set") {
      val set = SortedSet(
        Route(3, 3, Bus),
        Route(1, 2, Taxi),
        Route(2, 1, Underground),
        Route(1, 2, Taxi),
        Route(2, 1, Taxi)
      )

      set.toList should === (List(
        Route(1, 2, Taxi),
        Route(2, 1, Taxi),
        Route(2, 1, Underground),
        Route(3, 3, Bus)
      ))
    }

  }

  describe("Board") {

    val stations: Seq[Station] = List(1,2,3).map(Station(_))

    val routes: Seq[Route] = List(
      Route(1, 2, Taxi),
      Route(2, 1, Taxi),
      Route(1, 3, Bus),
      Route(3, 1, Bus),
      Route(2, 3, Taxi),
      Route(3, 2, Taxi)
    )

    val startingStations = List(Station(1),Station(2))

    val board: Board = new Board(stations, routes, startingStations)

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
