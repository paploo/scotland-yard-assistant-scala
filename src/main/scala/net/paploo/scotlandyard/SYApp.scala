package net.paploo.scotlandyard

import net.paploo.scotlandyard.board.Board

object SYApp {

  def main(args: Array[String]) {
    val board = Board.miltonBradley
    println(board)
    println(board.routes)
    println(board.graph.edges)
  }

}
