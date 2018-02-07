/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.oldplot

import com.cibo.evilplot.colors.Color
import com.cibo.evilplot.geometry.{Drawable, Extent, Group, Path, StrokeStyle}
import com.cibo.evilplot.numeric.{Bounds, Point}
import com.cibo.evilplot.plotdefs.{LinePlotDef, OneLinePlotData, PlotOptions}

case class Lines(
  chartAreaSize: Extent,
  lpd: LinePlotDef,
  xAxisDrawBounds: Bounds,
  yAxisDrawBounds: Bounds
) {
  private val xScale = chartAreaSize.width / xAxisDrawBounds.range
  private val yScale = chartAreaSize.height / yAxisDrawBounds.range
  private val pathSeq: Seq[Drawable] = lpd.lines.map { case OneLinePlotData(points: Seq[Point], color: Color, _) =>
    val scaledPoints =
      points.map(pt => Point((pt.x - xAxisDrawBounds.min) * xScale, (yAxisDrawBounds.max - pt.y) * yScale))
    StrokeStyle(Path(scaledPoints, 2.0), color)
  }
  def drawable: Drawable = Group(pathSeq)
}

// Draw a line plot consisting of a set of lines. Each Seq in `data` is a separate line. The colors Seq
case class LinePlot(chartSize: Extent, lines: LinePlotDef) extends Chart with ContinuousAxes {
    val options: PlotOptions = lines.options
    // these gets are safe given the objects they come from.
    val defaultXAxisBounds: Bounds = lines.xBounds.get
    val defaultYAxisBounds: Bounds = lines.yBounds.get
    def plottedData(extent: Extent): Drawable =
      Lines(extent, lines, xAxisDescriptor.axisBounds, yAxisDescriptor.axisBounds).drawable
}
