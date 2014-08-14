package net.paploo.scotlandyard.test.board

import net.paploo.scotlandyard.board.Board.Edition.{Ravensburger, MiltonBradley}
import net.paploo.scotlandyard.board.Route.TransitMode
import net.paploo.scotlandyard.board.Route.TransitMode._
import net.paploo.scotlandyard.board.Board.Ticket._
import net.paploo.scotlandyard.graph.{Path, NodeID}
import net.paploo.scotlandyard.test.SpecTest
import net.paploo.scotlandyard.board._
import net.paploo.scotlandyard.board.Board._

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

    describe("TransitMode") {

      val inputStrings = Seq("taxi", "Taxi", "bus", "Bus", "underground", "Underground", "subway", "Subway", "ferry", "Ferry", "boat", "Boat")
      val expectedObjects = Seq(Taxi, Taxi, Bus, Bus, Underground, Underground, Underground, Underground, Ferry, Ferry, Ferry, Ferry)

      it("should convert from string via partial function") {
        inputStrings.map(TransitMode.fromString) should === (expectedObjects)
      }

      it("should convert from string via apply") {
        inputStrings.map(TransitMode(_)) should === (expectedObjects)
      }

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
        paths.moveVia(BusTicket) should === (expected)
      }

      it("should transition via black ticket") {
        val expected: Seq[Path[Station, Route]] = Seq(
          Path(List(NodeID(2), NodeID(1))),
          Path(List(NodeID(3), NodeID(2)))
        )
        paths.moveVia(BlackTicket) === (expected)
      }

      it("should compose") {
        val expected: Seq[Path[Station, Route]] = Seq(
          Path(List(NodeID(3), NodeID(1), NodeID(2)))
        )

        val composedPaths = paths.moveVia(TaxiTicket).detectiveAt(3).moveVia(BusTicket)

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

    describe("Editions") {

      val inputStrings = Seq("miltonbradley", "MiltonBradley", "mb", "MB", "ravensburger", "Ravensburger", "rb")
      val expectedObjects = Seq(MiltonBradley, MiltonBradley, MiltonBradley, MiltonBradley, Ravensburger, Ravensburger, Ravensburger)

      it("should convert from string via partial function") {
        inputStrings.map(Edition.fromString) should === (expectedObjects)
      }

      it("should convert from string via apply") {
        inputStrings.map(Edition(_)) should === (expectedObjects)
      }

    }

    describe("Ticket") {

      val inputStrings = Seq("taxi", "TaxiTicket", "Taxi Ticket", "taxi ticket", "bus", "Bus Ticket", "UnderGround", "underground TICKET", "subway", "black", "BlACK TICKET")
      val expectedObjects = Seq(TaxiTicket, TaxiTicket, TaxiTicket, TaxiTicket, BusTicket, BusTicket, UndergroundTicket, UndergroundTicket, UndergroundTicket, BlackTicket, BlackTicket)

      it("should convert from string via partial function") {
        inputStrings.map(Ticket.fromString) should === (expectedObjects)
      }

      it("should convert from string via apply") {
        inputStrings.map(Ticket(_)) should === (expectedObjects)
      }

    }

  }

}
