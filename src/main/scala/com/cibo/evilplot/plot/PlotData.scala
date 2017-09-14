package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.{Drawable, Extent}

/** A class extending `PlotData` contains all data and plot-type specific options needed to create a plot of that type.
  *
  */
trait PlotData {
  def xBounds: Option[Bounds] = None
  def yBounds: Option[Bounds] = None
  def createPlot(extent: Extent, options: PlotOptions): Drawable
  def defaultAnnotationMaker: Seq[String] = Seq[String]()
}

