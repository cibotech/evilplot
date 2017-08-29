/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.plot

import com.cibo.evilplot.colors.{Color, White}
import com.cibo.evilplot.geometry._
import com.cibo.evilplot.layout.ChartLayout
import com.cibo.evilplot.numeric.AxisDescriptor
import com.cibo.evilplot.plot.ContinuousChartDistributable.{HorizontalGridLines, VerticalGridLines, XAxis, YAxis}
import com.cibo.evilplot.{StrokeStyle, Utils}
import org.scalajs.dom.CanvasRenderingContext2D


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

  override def createPlot(extent: Extent, options: PlotOptions): Drawable = {
    new LinePlot(extent, this, options)
  }
}

// Specify a sequence of lines to plot later, when the apply method is called
case class LinesLater(lpd: LinePlotData, xAxisDrawBounds: Bounds, yAxisDrawBounds: Bounds) extends DrawableLater {
  def apply(extent: Extent): Drawable = {
    val xScale = extent.width / xAxisDrawBounds.range
    val yScale = extent.height / yAxisDrawBounds.range
    val pathSeq: Seq[Drawable] = lpd.lines.map { case OneLinePlotData(points: Seq[Point], color: Color) =>
      val scaledPoints =
        points.map(pt => Point((pt.x - xAxisDrawBounds.min) * xScale, (yAxisDrawBounds.max - pt.y) * yScale))
      StrokeStyle(color)(Path(scaledPoints, 2.0))
    }
    Group(pathSeq: _*)
  }
}

// Draw a line plot consisting of a set of lines. Each Seq in `data` is a separate line. The colors Seq
// TODO: centralize code that originated with BarChart
class LinePlot(override val extent: Extent, lines: LinePlotData, options: PlotOptions)
  extends Drawable {
  val layout: Drawable = {
    val xBounds: Bounds = lines.xBounds.getOrElse(throw new IllegalArgumentException("must have xBounds"))
    val yBounds: Bounds = lines.yBounds.getOrElse(throw new IllegalArgumentException("must have yBounds"))
    val xAxisDrawBounds: Bounds = options.xAxisBounds.getOrElse(xBounds)
    val yAxisDrawBounds: Bounds = options.yAxisBounds.getOrElse(yBounds)
    val topLabel: DrawableLater = Utils.maybeDrawableLater(options.topLabel, (text: String) => Label(text))
    val rightLabel: DrawableLater = Utils.maybeDrawableLater(options.rightLabel,
      (text: String) => Label(text, rotate = 90))
    val xAxisDescriptor = AxisDescriptor(xAxisDrawBounds, options.numXTicks.getOrElse(10))
    val yAxisDescriptor = AxisDescriptor(yAxisDrawBounds, options.numYTicks.getOrElse(10))
    val xAxis = XAxis(xAxisDescriptor, label = options.xAxisLabel, options.drawXAxis)
    val yAxis = YAxis(yAxisDescriptor, label = options.yAxisLabel, options.drawYAxis)
    val linesLater = LinesLater(lines, xAxisDescriptor.axisBounds, yAxisDescriptor.axisBounds)
    val plotArea: DrawableLater = {
      def plotArea(extent: Extent): Drawable = {
        val xGridLines = Utils.maybeDrawable(options.xGridSpacing,
          (xGridSpacing: Double) => VerticalGridLines(xAxisDescriptor, xGridSpacing, color = White)(extent))
        val yGridLines = Utils.maybeDrawable(options.yGridSpacing,
          (yGridSpacing: Double) => HorizontalGridLines(yAxisDescriptor, yGridSpacing, color = White)(extent))
        Rect(extent) filled options.backgroundColor behind
          linesLater(extent) behind
          xGridLines behind yGridLines titled(options.title.getOrElse(""), 20.0)
      }
      new DrawableLaterMaker(plotArea)
    }
    val centerFactor = 0.85   // proportion of the plot to allocate to the center
    new ChartLayout(extent, preferredSizeOfCenter = extent * centerFactor, center = plotArea,
      left = yAxis, bottom = xAxis, top = topLabel, right = rightLabel)
  }

  override def draw(canvas: CanvasRenderingContext2D): Unit = layout.draw(canvas)
}
