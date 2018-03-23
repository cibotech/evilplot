package com.cibo.evilplot.plot.aesthetics

import scala.annotation.implicitNotFound

/**
  * Themes are a mechanism for controlling the styling of plots from a single object.
  * A theme is defined implicitly and passed into plots.
  * @see [[DefaultTheme]] for an easy default.
  **/
@implicitNotFound("No implicit Theme found. You may wish to import com.cibo.evilplot.plot.aesthetics.DefaultTheme._")
trait Theme {
  val fonts: Fonts
  val colors: Colors
  val elements: Elements
}
