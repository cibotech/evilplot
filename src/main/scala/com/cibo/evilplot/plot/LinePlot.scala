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

/*
// Plot paths connecting the points in data. Each Seq[Point] makes a path, drawn using the corresponding color.
class LinePlot(override val extent: Extent, data: Seq[Seq[Point]], colors: Seq[Color], options: PlotOptions)
  extends Drawable {
  require(data.length == colors.length)
  val layout: Drawable = {
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
    val chartArea: DrawableLater = {
      def chartArea(extent: Extent): Drawable = {
        val xGridLines = Utils.maybeDrawable(options.xGridSpacing,
          (xGridSpacing: Double) => VerticalGridLines(xTicks, xGridSpacing, color = White)(extent))
        val yGridLines = Utils.maybeDrawable(options.yGridSpacing,
          (yGridSpacing: Double) => HorizontalGridLines(yTicks, yGridSpacing, color = White)(extent))
        val xScale = extent.width / xAxisDrawBounds.range
        val yScale = extent.height / yAxisDrawBounds.range
        val paths: Seq[Drawable] = (data zip colors).map { case (_data: Seq[Point], color: Color) =>
          FlipY(Scale(xScale, yScale)(StrokeStyle(color)(Path(_data, strokeWidth = 0.1))))
        }
        val groupedPaths = Group(paths: _*)

        //val scaledPaths = Scale(xScale, yScale)(groupedPaths)
        val scaledPaths = groupedPaths
        //val scaledPaths = FlipY(Scale(xScale, yScale)(groupedPaths transY yMax * yScale))
        //val scaledPaths = groupedPaths transY yAxisDrawBounds.range * yScale
        Rect(extent) filled options.backgroundColor behind
          scaledPaths behind xGridLines behind yGridLines
      }

      new DrawableLaterMaker(chartArea)
    }
    ChartLayout(extent, preferredSizeOfCenter = centerExtent, center = chartArea, left = yAxis, bottom = xAxis)
  }

  override def draw(canvas: CanvasRenderingContext2D): Unit = layout.draw(canvas)

}
*/

/*
// Plot paths connecting the points in data. Each Seq[Point] makes a path, drawn using the corresponding color.
class LinePlot(override val extent: Extent, data: Seq[Seq[Point]], colors: Seq[Color], options: PlotOptions)
  extends Drawable {
  require(data.length == colors.length)
  val layout: Drawable = {
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
    val xGridLines = Utils.maybeDrawable(options.xGridSpacing,
      (xGridSpacing: Double) => VerticalGridLines(xTicks, xGridSpacing, color = White)(extent))
    val yGridLines = Utils.maybeDrawable(options.yGridSpacing,
      (yGridSpacing: Double) => HorizontalGridLines(yTicks, yGridSpacing, color = White)(extent))
    val xScale = extent.width / xAxisDrawBounds.range
    val yScale = extent.height / yAxisDrawBounds.range
    val pathSeq: Seq[Drawable] = (data zip colors).map { case (_data: Seq[Point], color: Color) =>
      //FlipY(Scale(xScale, yScale)(StrokeStyle(color)(Path(_data, strokeWidth = 0.1))))
      FlipY(Scale(100, 100)(StrokeStyle(color)(Path(_data, strokeWidth = 0.1))))
    }
    val paths = Group(pathSeq: _*)

    //val scaledPaths = Scale(xScale, yScale)(groupedPaths)
    val scaledPaths = paths
    //val scaledPaths = FlipY(Scale(xScale, yScale)(groupedPaths transY yMax * yScale))
    //val scaledPaths = groupedPaths transY yAxisDrawBounds.range * yScale
//    Rect(extent) filled options.backgroundColor behind
//      scaledPaths behind xGridLines behind yGridLines
    val _yAxis = yAxis(extent)
//    (_yAxis beside paths) above (xAxis(extent) padLeft _yAxis.extent.width)
    paths
  }

  override def draw(canvas: CanvasRenderingContext2D): Unit = layout.draw(canvas)
}
*/

case class LineToPlot(points: Seq[Point], color: Color) {
  def xBounds: Bounds = {
    val xS = points.map(_.x)
    val xMin = xS.reduce[Double](math.min)
    val xMax = xS.reduce[Double](math.max)
    Bounds(xMin, xMax)
  }

  def yBounds: Bounds = {
    val yS = points.map(_.y)
    val yMin = yS.reduce[Double](math.min)
    val yMax = yS.reduce[Double](math.max)
    Bounds(yMin, yMax)
  }
}

object LineToPlot {
  def xBounds(lines: Seq[LineToPlot]): Bounds = {
    val bounds = lines.map(_.xBounds)
    val xMin = bounds.map(_.min).reduce[Double](math.min)
    val xMax = bounds.map(_.max).reduce[Double](math.max)
    Bounds(xMin, xMax)
  }

  def yBounds(lines: Seq[LineToPlot]): Bounds = {
    val bounds = lines.map(_.yBounds)
    val yMin = bounds.map(_.min).reduce[Double](math.min)
    val yMax = bounds.map(_.max).reduce[Double](math.max)
    Bounds(yMin, yMax)
  }
}


// Draw a line plot consisting of a set of lines. Each Seq in `data` is a separate line. The colors Seq
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

    // TODO: centralize code that originated with BarChart
    val xTicks = Ticks(xAxisDrawBounds, options.numXTicks.getOrElse(10))
    val yTicks = Ticks(yAxisDrawBounds, options.numYTicks.getOrElse(10))




//    val bars = Bars(xBounds, Some(xAxisDrawBounds), yAxisDrawBounds, data, options.barColor)
    val xAxis = XAxis(xTicks, label = options.xAxisLabel, options.drawXAxis)
    val yAxis = YAxis(yTicks, label = options.yAxisLabel, options.drawYAxis)
    val chartArea: DrawableLater = {
      def chartArea(extent: Extent): Drawable = {
        val translatedAnnotation = Utils.maybeDrawable(options.annotation,
          (annotation: ChartAnnotation) =>
            ((annotation transX annotation.position._1 * extent.width)
              transY (annotation.position._2 * extent.height)))
        val xGridLines = Utils.maybeDrawable(options.xGridSpacing,
          (xGridSpacing: Double) => VerticalGridLines(xTicks, xGridSpacing, color = White)(extent))
        val yGridLines = Utils.maybeDrawable(options.yGridSpacing,
          (yGridSpacing: Double) => HorizontalGridLines(yTicks, yGridSpacing, color = White)(extent))
        Rect(extent) filled options.backgroundColor behind
          //bars(extent) behind
          xGridLines behind yGridLines behind translatedAnnotation
      }
      new DrawableLaterMaker(chartArea)
    }
    val centerFactor = 0.85   // proportion of the plot to allocate to the center
    new ChartLayout(extent, preferredSizeOfCenter = extent * centerFactor, center = chartArea, left = yAxis, bottom = xAxis,
      top = topLabel, right = rightLabel)
  }

  override def draw(canvas: CanvasRenderingContext2D): Unit = layout.draw(canvas)
}
