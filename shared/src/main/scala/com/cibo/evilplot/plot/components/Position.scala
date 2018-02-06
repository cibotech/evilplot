package com.cibo.evilplot.plot.components

/** The position of a plot component. */
sealed trait Position

object Position {
  case object Top extends Position
  case object Bottom extends Position
  case object Left extends Position
  case object Right extends Position
  case object Overlay extends Position
  case object Background extends Position
}
