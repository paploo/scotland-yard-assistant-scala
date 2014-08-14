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
    def apply(name: String): TransitMode = (Map(
      "Taxi" -> Taxi,
      "Bus" -> Bus,
      "Underground" -> Underground,
      "Ferry" -> Ferry
    ) orElse errorPartial)(name)

    val errorPartial: PartialFunction[String, TransitMode] = {case name => throw new java.lang.IllegalArgumentException(s"Unrecognized transit mode $name")}

    //TODO: Synchronize the use of partial function, apply, and erroring between this, editions, and tickets.

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

    def apply(str: String): Edition = if (nameMap.isDefinedAt(str.toLowerCase)) nameMap(str.toLowerCase)
    else throw new IllegalArgumentException(s"No Edition for String $str")

    //TODO: Change to a partial function and downcase.
    val nameMap: Map[String, Edition] = Map(
      "miltonbradley" -> MiltonBradley,
      "ravensburger" -> Ravensburger
    )
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

    //TODO: Change to a partial function and downcase.
    val nameMap: Map[String, Ticket] = Map(
      "taxi" -> TaxiTicket,
      "bus" -> BusTicket,
      "underground" -> UndergroundTicket,
      "blackticket" -> BlackTicket
    )
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
