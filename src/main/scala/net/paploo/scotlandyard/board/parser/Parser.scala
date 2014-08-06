package net.paploo.scotlandyard.board.parser

import java.io._
import java.nio.charset.StandardCharsets
import net.paploo.scotlandyard.board.parser.Parser.ParseException

import scala.annotation.tailrec
import scala.collection.SortedSet
import net.paploo.scotlandyard.board.{Station, Route, Board}
import net.paploo.scotlandyard.board.Route.TransitMode

import scala.util.{Failure, Try}

object Parser {
  implicit class ParserWithDecoder(val decoder: Decoder) extends Parser

  object CSV {
    def apply(input: String): Parser = apply( new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)) )
    def apply(file: File): Parser = apply( new FileInputStream(file) )
    def apply(ioStream: InputStream): Parser = new ParserWithDecoder( new CSVDecoder(ioStream) )
  }

  class ParseException(val cause: Throwable, val lineNumber: Option[Int] = None) extends java.lang.RuntimeException(cause) {
    override val toString: String = if(lineNumber.isDefined) super.toString + s" on line ${lineNumber.get}" else super.toString
  }
}

trait Parser {
  def decoder: Decoder

  def board: Board = {
    val stationsAndRoutes = aggregate(decoder.routes)
    new Board(stationsAndRoutes._1, stationsAndRoutes._2, decoder.startingStations.toList)
  }

  def errors: Seq[Throwable] = decoder.errors.toSeq

  @tailrec
  final def aggregate(inputRoutes: Seq[Route], stationSet: SortedSet[Station] = SortedSet.empty, routeSet: SortedSet[Route] = SortedSet.empty): (Seq[Station], Seq[Route]) = {
    if (inputRoutes.isEmpty) {
      (stationSet.toList, routeSet.toList)
    } else {
      val inputRoute = inputRoutes.head
      val srcStation = Station(inputRoute.sourceNum)
      val destStation = Station(inputRoute.destinationNum)
      val stations = SortedSet(srcStation, destStation)
      aggregate(inputRoutes.tail, stationSet ++ stations, routeSet + inputRoute)
    }
  }
}

trait Decoder {
  def startingStations: Stream[Station]
  def routes: Stream[Route]
  def errors: Stream[Throwable]
}

abstract class InputStreamDecoder(ioStream: InputStream) extends Decoder {
  def tokens: Stream[Try[Token]]
}

sealed trait Token {
  def station: Option[Station]
  def route: Option[Route]
}

object Token {
  case class StationToken(station: Option[Station]) extends Token { override val route = None }
  case class RouteToken(route: Option[Route]) extends Token { override val station = None }
}

class CSVDecoder(ioStream: InputStream) extends InputStreamDecoder(ioStream) {

  val reader = new BufferedReader(new InputStreamReader(ioStream))

  override val tokens: Stream[Try[Token]] = tokenStreamer(reader)

  override val startingStations: Stream[Station] = tokens.filter(t => t.isSuccess && t.get.station.isDefined).map(_.get.station.get)

  override val routes: Stream[Route] = tokens.filter(t => t.isSuccess && t.get.route.isDefined).map(_.get.route.get)

  override val errors: Stream[Throwable] = tokens.filter(_.isFailure).map(_.failed.get)

  def token(line: String): Token = line.split(",\\s*").toList match {
    case List(num) => Token.StationToken( Some(Station(num.toInt)) )
    case List(src, dest, mode) => Token.RouteToken( Some(Route(src.toInt, dest.toInt, TransitMode(mode))) )
    case s => throw new java.lang.RuntimeException(s"Unparsable Line: $s")
  }

  def tokenStreamer(reader: BufferedReader, lineNumber: Int = 1): Stream[Try[Token]] = {
    val line = reader.readLine()
    if (line == null) Stream.empty else Try(token(line)).recoverWith { case t => Failure(new ParseException(t, Some(lineNumber)))} #:: tokenStreamer(reader, lineNumber+1)
  }

}
