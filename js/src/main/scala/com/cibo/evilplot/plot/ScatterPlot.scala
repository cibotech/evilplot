package com.cibo.evilplot.plot

import com.cibo.evilplot.colors.Colors.{ScaledColorBar, SingletonColorBar}
import com.cibo.evilplot.geometry.{Debug, Disc, Drawable, EmptyDrawable, Extent}
import com.cibo.evilplot.numeric.{Bounds, Point}
import com.cibo.evilplot.plotdefs.{PlotOptions, ScatterPlotDef}

class ScatterPlot(val chartSize: Extent, definition: ScatterPlotDef)
  extends Chart with ContinuousAxes {
  val options: PlotOptions = definition.options
  private val data = definition.data
  val defaultXAxisBounds: Bounds = Bounds(data.minBy(_.x).x, data.maxBy(_.x).x)
  val defaultYAxisBounds: Bounds = Bounds(data.minBy(_.y).y, data.maxBy(_.y).y)

  // Will return an EmptyDrawable if point is out-of-bounds.
  private[plot] def scatterPoint(x: Double, y: Double)(scaleX: Double, scaleY: Double): Drawable = {
    if (xAxisDescriptor.axisBounds.isInBounds(x) && yAxisDescriptor.axisBounds.isInBounds(y))
      Disc(definition.pointSize,
        (x - xAxisDescriptor.axisBounds.min) * scaleX, (yAxisDescriptor.axisBounds.max - y) * scaleY)
    else EmptyDrawable()
  }

  def plottedData(extent: Extent): Drawable = {
    val scaleX: Double = extent.width / xAxisDescriptor.axisBounds.range
    val scaleY: Double = extent.height / yAxisDescriptor.axisBounds.range

    val points = (definition.zData, definition.colorBar) match {
      case (Some(_zData), _colorBar: ScaledColorBar) =>
        require(_zData.length == data.length, "color and point data must have same length")
        (data zip _zData).map { case (Point(x, y), z) =>
          scatterPoint(x, y)(scaleX, scaleY) filled _colorBar.getColor(z)
        }
      case (_, SingletonColorBar(color)) =>
        data.map { case Point(x, y) => scatterPoint(x, y)(scaleX, scaleY) filled color }
      case (_, _) => throw new IllegalArgumentException
    }
    points.group
  }
}
