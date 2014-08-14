package net.paploo.scotlandyard.board

import net.paploo.scotlandyard.board.Route.TransitMode
import net.paploo.scotlandyard.board.parser.Parser
import net.paploo.scotlandyard.graph._

object Route {

  sealed trait TransitMode {
    def name: String
    def value: Int
  }

  object TransitMode {
    def apply(name: String): TransitMode = (fromString orElse fromStringErrorPartial)(name)

    val fromString: PartialFunction[String, TransitMode] = {
      case s if s.toLowerCase == "taxi" => Taxi
      case s if s.toLowerCase == "bus" => Bus
      case s if s.toLowerCase == "underground" => Underground
      case s if s.toLowerCase == "subway" => Underground
      case s if s.toLowerCase == "ferry" => Ferry
      case s if s.toLowerCase == "boat" => Ferry
    }

    protected val fromStringErrorPartial: PartialFunction[String, TransitMode] = {case name => throw new java.lang.IllegalArgumentException(s"Unrecognized transit mode $name")}

    case object Taxi extends TransitMode { val name = "Taxi"; val value = 1 }
    case object Bus extends TransitMode { val name = "Bus"; val value = 2 }
    case object Underground extends TransitMode { val name = "Underground"; val value = 4 }
    case object Ferry extends TransitMode { val name = "Ferry"; val value = 8 }
    case class Unknown(name: String) extends TransitMode { val value = 99 }
  }

  implicit def ordering: Ordering[Route] = Ordering.by(r => (r.sourceNum, r.destinationNum, r.transitMode.value))

}

case class Route(sourceNum: Int, destinationNum: Int, transitMode: Route.TransitMode) {
  def toEdge: Edge[Station, Route] = new Edge(NodeID(sourceNum), NodeID(destinationNum), this)
}

case object Station {

  implicit def ordering: Ordering[Station] = Ordering.by(s => s.num)

}

case class Station(num: Int) {
  def toNode: Node[Station, Route] = new Node(NodeID(num), this)
}

object Board {

  lazy val miltonBradley: Board = loadBoardResource("/miltonbradley.csv")

  lazy val ravensburger: Board = loadBoardResource("/ravensburger.csv")

  final private def loadBoardResource(path: String): Board =  {
      val stream = getClass.getResourceAsStream(path)
      val p = Parser.CSV(stream)
      if (p.errors.nonEmpty) p.errors.foreach(System.err.println)
      p.board
    }

  def apply(edition: Edition): Option[Board] = edition match {
    case Edition.MiltonBradley => Some(miltonBradley)
    case Edition.Ravensburger => Some(ravensburger)
    case _ => None
  }

  sealed trait Edition

  object Edition {
    case object MiltonBradley extends Edition
    case object Ravensburger extends Edition

    def apply(name: String): Edition = (fromString orElse fromStringErrorPartial)(name)

    val fromString: PartialFunction[String, Edition] = {
      case s if s.toLowerCase == "miltonbradley" => MiltonBradley
      case s if s.toLowerCase == "mb" => MiltonBradley
      case s if s.toLowerCase == "ravensburger" => Ravensburger
      case s if s.toLowerCase == "rb" => Ravensburger
    }

    protected val fromStringErrorPartial: PartialFunction[String, Edition] = {case str => throw new IllegalArgumentException(s"No Edition for String $str")}
  }

  object Graph {
    def apply(stations: Seq[Station], routes: Seq[Route]): Graph[Station, Route] = new Graph(stations.map(_.toNode), routes.map(_.toEdge))
  }

  sealed trait Ticket { def transitModes: Seq[TransitMode] }

  object Ticket {
    case object TaxiTicket extends Ticket { val transitModes = Seq(TransitMode.Taxi)}
    case object BusTicket extends Ticket { val transitModes = Seq(TransitMode.Bus)}
    case object UndergroundTicket extends Ticket { val transitModes = Seq(TransitMode.Underground)}
    case object BlackTicket extends Ticket { val transitModes = Seq(TransitMode.Taxi, TransitMode.Bus, TransitMode.Underground, TransitMode.Ferry) }

    def apply(name: String): Ticket = (fromString orElse fromStringErrorPartial)(name)

    val fromString: PartialFunction[String, Ticket] = {
      case s if s.toLowerCase matches """(?i)^(taxi)(\s*ticket)?$""" => TaxiTicket
      case s if s.toLowerCase matches """(?i)^(bus)(\s*ticket)?$""" => BusTicket
      case s if s.toLowerCase matches """(?i)^(underground|subway)(\s*ticket)?$""" => UndergroundTicket
      case s if s.toLowerCase matches """(?i)^(black)(\s*ticket)?$""" => BlackTicket
    }

    protected val fromStringErrorPartial: PartialFunction[String, Ticket] = {case str => throw new IllegalArgumentException(s"No Ticket for String $str")}
  }

  implicit class BoardPath(val paths: Seq[Path[Station, Route]]) {

    def moveVia(ticket: Ticket)(implicit graph: Graph[Station, Route]): Seq[Path[Station, Route]] = paths.transitionEdges(e => ticket.transitModes.contains(e.data.transitMode))

    def detectiveAt(stationNum: Int)(implicit graph: Graph[Station, Route]): Seq[Path[Station, Route]] = paths.filterNodes(_.data.num != stationNum)

    def mrXAt(stationNum: Int)(implicit graph: Graph[Station, Route]): Seq[Path[Station, Route]] = paths.filterNodes(_.data.num == stationNum)

  }

}

class Board(val stations: Seq[Station], val routes: Seq[Route], val startingStations: Seq[Station]) {

  val graph: Graph[Station, Route] = Board.Graph(stations, routes)

  val startingNodeIDs: Seq[NodeID] = startingStations.map(_.toNode.id)

  val startingPaths: Seq[Path[Station,Route]] = startingNodeIDs.map(id => new Path(List(id)))

  override def toString = s"Board(graph = $graph, startingNodeIDs = $startingNodeIDs)"

}
