/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.plot

import com.cibo.evilplot.{StrokeStyle, Utils}
import com.cibo.evilplot.colors.{Color, White}
import com.cibo.evilplot.geometry._
import com.cibo.evilplot.layout.ChartLayout
import com.cibo.evilplot.numeric.Ticks
import com.cibo.evilplot.plot.ContinuousChartDistributable.{HorizontalGridLines, VerticalGridLines, XAxis, YAxis}
import org.scalajs.dom.CanvasRenderingContext2D

case class LineToPlot(points: Seq[Point], color: Color) {
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

object LineToPlot {
  def xBounds(lines: Seq[LineToPlot]): Bounds = {
    val bounds = lines.map(_.xBounds)
    val xMin = bounds.map(_.min).min
    val xMax = bounds.map(_.max).max
    Bounds(xMin, xMax)
  }

  def yBounds(lines: Seq[LineToPlot]): Bounds = {
    val bounds = lines.map(_.yBounds)
    val yMin = bounds.map(_.min).min
    val yMax = bounds.map(_.max).max
    Bounds(yMin, yMax)
  }
}

case class LinesLater(lines: Seq[LineToPlot], xBounds: Bounds, yBounds: Bounds, xAxisDrawBounds: Bounds, yAxisDrawBounds: Bounds) extends DrawableLater {
  def apply(extent: Extent): Drawable = {
    val xScale = extent.width / xAxisDrawBounds.range
    val yScale = extent.height / yAxisDrawBounds.range
    val pathSeq: Seq[Drawable] = lines.map { case LineToPlot(points: Seq[Point], color: Color) =>
      val scaledPoints =
        points.map(pt => Point((pt.x - xAxisDrawBounds.min) * xScale, (yAxisDrawBounds.max - pt.y) * yScale))
      StrokeStyle(color)(Path(scaledPoints, 2.0))
    }
    Group(pathSeq: _*)
  }
}

// Draw a line plot consisting of a set of lines. Each Seq in `data` is a separate line. The colors Seq
// TODO: centralize code that originated with BarChart
class LinePlot(override val extent: Extent, lines: Seq[LineToPlot], options: PlotOptions)
  extends Drawable {
  val layout: Drawable = {
    val xBounds = LineToPlot.xBounds(lines)
    val yBounds = LineToPlot.yBounds(lines)
    val xAxisDrawBounds: Bounds = options.xAxisBounds.getOrElse(xBounds)
    val yAxisDrawBounds: Bounds = options.yAxisBounds.getOrElse(yBounds)
    val topLabel: DrawableLater = Utils.maybeDrawableLater(options.topLabel, (text: String) => Label(text))
    val rightLabel: DrawableLater = Utils.maybeDrawableLater(options.rightLabel,
      (text: String) => Label(text, rotate = 90))
    val xTicks = Ticks(xAxisDrawBounds, options.numXTicks.getOrElse(10))
    val yTicks = Ticks(yAxisDrawBounds, options.numYTicks.getOrElse(10))
    val xAxis = XAxis(xTicks, label = options.xAxisLabel, options.drawXAxis)
    val yAxis = YAxis(yTicks, label = options.yAxisLabel, options.drawYAxis)
    val linesLater = LinesLater(lines, xBounds, yBounds, xAxisDrawBounds, yAxisDrawBounds)
    val plotArea: DrawableLater = {
      def plotArea(extent: Extent): Drawable = {
        val xGridLines = Utils.maybeDrawable(options.xGridSpacing,
          (xGridSpacing: Double) => VerticalGridLines(xTicks, xGridSpacing, color = White)(extent))
        val yGridLines = Utils.maybeDrawable(options.yGridSpacing,
          (yGridSpacing: Double) => HorizontalGridLines(yTicks, yGridSpacing, color = White)(extent))
        Rect(extent) filled options.backgroundColor behind
          linesLater(extent) behind
          xGridLines behind yGridLines
      }
      new DrawableLaterMaker(plotArea)
    }
    val centerFactor = 0.85   // proportion of the plot to allocate to the center
    new ChartLayout(extent, preferredSizeOfCenter = extent * centerFactor, center = plotArea,
      left = yAxis, bottom = xAxis, top = topLabel, right = rightLabel)
    //linesLater(extent)
  }

  override def draw(canvas: CanvasRenderingContext2D): Unit = layout.draw(canvas)
}
