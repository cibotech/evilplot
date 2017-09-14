/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.plot

import com.cibo.evilplot.colors.Color
import com.cibo.evilplot.geometry.{Drawable, Extent, Group, Path, WrapDrawable}
import com.cibo.evilplot.numeric.{Bounds, Point}
import com.cibo.evilplot.StrokeStyle


case class OneLinePlotData(points: Seq[Point], color: Color) {
  def xBounds: Bounds = {
    val xS = points.map(_.x)
    val xMin = xS.min
    val xMax = xS.max
    Bounds(xMin, xMax)
  }

  def yBounds: Bounds = {
    val yS = points.map(_.y)
    val yMin = yS.min
    val yMax = yS.max
    Bounds(yMin, yMax)
  }
}

case class LinePlotData(lines: Seq[OneLinePlotData]) extends PlotData {
  override def xBounds: Option[Bounds] = {
    val bounds = lines.map(_.xBounds)
    val xMin = bounds.map(_.min).min
    val xMax = bounds.map(_.max).max
    Some(Bounds(xMin, xMax))
  }

  override def yBounds: Option[Bounds] = {
    val bounds = lines.map(_.yBounds)
    val yMin = bounds.map(_.min).min
    val yMax = bounds.map(_.max).max
    Some(Bounds(yMin, yMax))
  }

  override def createPlot(extent: Extent, options: PlotOptions): Chart = {
    new LinePlot(extent, this, options)
  }
}

case class Lines(chartAreaSize: Extent, lpd: LinePlotData,
                 xAxisDrawBounds: Bounds, yAxisDrawBounds: Bounds) extends WrapDrawable {
  private val xScale = chartAreaSize.width / xAxisDrawBounds.range
  private val yScale = chartAreaSize.height / yAxisDrawBounds.range
  private val pathSeq: Seq[Drawable] = lpd.lines.map { case OneLinePlotData(points: Seq[Point], color: Color) =>
    val scaledPoints =
      points.map(pt => Point((pt.x - xAxisDrawBounds.min) * xScale, (yAxisDrawBounds.max - pt.y) * yScale))
    StrokeStyle(color)(Path(scaledPoints, 2.0))
  }
  override def drawable: Drawable = Group(pathSeq: _*)
}

// Draw a line plot consisting of a set of lines. Each Seq in `data` is a separate line. The colors Seq
class LinePlot(val chartSize: Extent, lines: LinePlotData, val options: PlotOptions) extends Chart with ContinuousAxes {
    // these gets are safe given the objects they come from.
    val defaultXAxisBounds: Bounds = lines.xBounds.get
    val defaultYAxisBounds: Bounds = lines.yBounds.get
    def plottedData(extent: Extent): Drawable =
      Lines(extent, lines, xAxisDescriptor.axisBounds, yAxisDescriptor.axisBounds)
}
