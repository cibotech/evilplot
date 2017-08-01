/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.{Drawable, Extent, Fit, FlipY, Point, Segment, WrapDrawable}


// TODO: use a layout manager to abstract out common logic
class LinePlot(override val extent: Extent, data: Seq[Point], options: PlotOptions)
  extends WrapDrawable {

  private val _drawable = {
    val xvals = data.map(_.x)
    val yvals = data.map(_.y)
    val xMin = xvals.reduce(math.min)
    val xMax = xvals.reduce(math.max)
    val xBounds = Bounds(xMin, xMax)
    val yMin = yvals.reduce(math.min)
    val yMax = yvals.reduce(math.max)
    val yBounds = Bounds(yMin, yMax)
    val xAxisDrawBounds: Bounds = options.xAxisBounds.getOrElse(xBounds)
    val yAxisDrawBounds = options.yAxisBounds.getOrElse(Bounds(yMin, yMax))

    // TODO: fix the XAxis and YAxis extents
    val xAxis = new XAxis(extent, xMin, xMax, options.numXTicks.getOrElse(4))
    val yAxis = new YAxis(extent, yAxisDrawBounds.min, yAxisDrawBounds.max, options.numYTicks.getOrElse(4))



    Fit(extent)(FlipY(Segment(data, strokeWidth = 0.1)))
  }



  def drawable: Drawable = _drawable

}
