package com.cibo.evilplot.plot.aesthetics

import scala.annotation.implicitNotFound

@implicitNotFound("No implicit Theme found. You may wish to import com.cibo.evilplot.plot.aesthetics.DefaultTheme._")
trait Theme {
  val fonts: Fonts
  val colors: Colors
  val elements: Elements
}
