package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.Extent
import com.cibo.evilplot.numeric.Bounds

/** A class extending `PlotData` contains all data and plot-type specific options needed to create a plot of that type.
  *
  */
trait PlotData {
  def xBounds: Option[Bounds] = None
  def yBounds: Option[Bounds] = None
  def createPlot(extent: Extent, options: PlotOptions): Chart
  def defaultAnnotationMaker: Seq[String] = Seq[String]()
}

