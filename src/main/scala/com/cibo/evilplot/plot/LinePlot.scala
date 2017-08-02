/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.{Drawable, Extent, Fit, FlipY, Point, Scale, Segment, WrapDrawable}
import org.scalajs.dom.CanvasRenderingContext2D


// TODO: use a layout manager to abstract out common logic
class LinePlot(override val extent: Extent, data: Seq[Point], options: PlotOptions)
  extends Drawable {

  private val _drawable = {
    val xvals: Seq[Double] = data.map(_.x)
    val yvals: Seq[Double] = data.map(_.y)
    val xMin = xvals.reduce[Double](math.min)
    val xMax = xvals.reduce[Double](math.max)
    val yMin = yvals.reduce[Double](math.min)
    val yMax = yvals.reduce[Double](math.max)
    val yAxisDrawBounds = options.yAxisBounds.getOrElse(Bounds(yMin, yMax))

    val xAxis = new XAxis(extent, xMin, xMax, options.numXTicks.getOrElse(4))
    val yAxis = new YAxis(extent, yAxisDrawBounds.min, yAxisDrawBounds.max, options.numYTicks.getOrElse(4))

    val xscale = extent.width / (xMax - xMin)
    val segment = Scale(xscale, xscale)(FlipY(Segment(data, strokeWidth = 0.1)))
    val assembled = yAxis beside (xAxis below segment)

    Fit(extent)(assembled)
  }

  override def draw(canvas: CanvasRenderingContext2D) = _drawable.draw(canvas)
}
