package net.paploo.scotlandyard.test.board.parser

import net.paploo.scotlandyard.board.Route.TransitMode.{Bus, Taxi}
import net.paploo.scotlandyard.board.{Route, Station}
import net.paploo.scotlandyard.board.parser.Parser
import net.paploo.scotlandyard.test.SpecTest

class ParserTest extends SpecTest {

  describe("CSV Parser") {

    val csvString = "1\n2\n1,2,Taxi\n2,1,Taxi\n1, 2, Taxi\n3,3,Eeloo\na,b,c\n1,3,Bus"

    lazy val parser = Parser.CSV(csvString)

    lazy val board = parser.board

    it("should produce a board with the right startingStations") {
      val startingStations = board.startingStations
      val expected = List(Station(1), Station(2))
      startingStations should === (expected)
    }

    it("should produce a board with the right stations") {
      val stations = board.stations
      val expected = List(Station(1), Station(2), Station(3))
      stations should === (expected)
    }

    it("should produce a board with the right routes") {
      val routes = board.routes
      val expected = List(
        Route(1, 2, Taxi),
        Route(1, 3, Bus),
        Route(2, 1, Taxi)
      )
      routes should === (expected)
    }

    it("should produce the appropriate list of errors") {
      val errors = parser.errors
      errors.length should === (2)
    }

  }

}
