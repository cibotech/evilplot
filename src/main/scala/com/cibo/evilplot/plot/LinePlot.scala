/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.plot

import com.cibo.evilplot.{StrokeStyle, Utils}
import com.cibo.evilplot.colors.{Color, White}
import com.cibo.evilplot.geometry._
import com.cibo.evilplot.layout.ChartLayout
import com.cibo.evilplot.numeric.Ticks
import org.scalajs.dom.CanvasRenderingContext2D


// Plot paths connecting the points in data. Each Seq[Point] makes a path, drawn using the corresponding color.
class LinePlot(override val extent: Extent, data: Seq[Seq[Point]], colors: Seq[Color], options: PlotOptions)
  extends Drawable {
  require(data.length == colors.length)
  val layout: Drawable = {
    val paths: Seq[Drawable] = (data zip colors).map { case (_data: Seq[Point], color: Color) =>
      FlipY(StrokeStyle(color)(Path(_data, strokeWidth = 0.1)))
    }
    val groupedPaths = Group(paths: _*)
    val xvals: Seq[Double] = data.flatMap(_.map(_.x))
    val xMin: Double = xvals.reduce[Double](math.min)
    val xMax: Double = xvals.reduce[Double](math.max)
    val yvals: Seq[Double] = data.flatMap(_.map(_.y))
    val yMin: Double = yvals.reduce[Double](math.min)
    val yMax: Double = yvals.reduce[Double](math.max)
    val xAxisDrawBounds: Bounds = options.xAxisBounds.getOrElse(Bounds(xMin, xMax))
    val yAxisDrawBounds: Bounds = options.yAxisBounds.getOrElse(Bounds(yMin, yMax))
    val xTicks = Ticks(xAxisDrawBounds, options.numXTicks.getOrElse(10))
    val yTicks = Ticks(yAxisDrawBounds, options.numYTicks.getOrElse(10))
    val xAxis = XAxis(xTicks)
    val yAxis = YAxis(yTicks)
    val centerFactor = 0.85   // proportion of the plot to allocate to the center
    val centerExtent = extent * centerFactor
    val scale = math.min(extent.width / (xMax - xMin), extent.height / (yMax - yMin))
    val scaledPaths = Scale(scale, scale)(groupedPaths)
    val chartArea: DrawableLater = {
      def chartArea(extent: Extent): Drawable = {
        val xGridLines = Utils.maybeDrawable(options.xGridSpacing,
          (xGridSpacing: Double) => VerticalGridLines(xTicks, xGridSpacing, color = White)(extent))
        val yGridLines = Utils.maybeDrawable(options.yGridSpacing,
          (yGridSpacing: Double) => HorizontalGridLines(yTicks, yGridSpacing, color = White)(extent))
        Rect(extent) filled options.backgroundColor behind
          scaledPaths behind xGridLines behind yGridLines
      }

      new DrawableLaterMaker(chartArea)
    }
    ChartLayout(extent, preferredSizeOfCenter = centerExtent, center = chartArea, left = yAxis, bottom = xAxis)
  }

  override def draw(canvas: CanvasRenderingContext2D): Unit = layout.draw(canvas)

}
