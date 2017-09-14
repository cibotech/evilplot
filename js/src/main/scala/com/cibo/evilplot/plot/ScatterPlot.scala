package com.cibo.evilplot.plot

import com.cibo.evilplot.colors.Black
import com.cibo.evilplot.colors.Colors.{ColorBar, ScaledColorBar, SingletonColorBar}
import com.cibo.evilplot.geometry.{Disc, Drawable, EmptyDrawable, Extent, Translate}
import com.cibo.evilplot.numeric.{Bounds, Point}

case class ScatterPlotData(data: Seq[Point], zData: Option[Seq[Double]], pointSize: Double = 2.25,
                           colorBar: ColorBar = SingletonColorBar(Black)) extends PlotData {
  override def xBounds: Option[Bounds] = Some(Bounds(data.minBy(_.x).x, data.maxBy(_.x).x))
  override def yBounds: Option[Bounds] = Some(Bounds(data.minBy(_.y).y, data.maxBy(_.y).y))
  override def createPlot(extent: Extent, options: PlotOptions): Chart = {
    new ScatterPlot(extent, data, zData, options, pointSize = pointSize, colorBar = colorBar)
  }
}

class ScatterPlot(val chartSize: Extent, data: Seq[Point], zData: Option[Seq[Double]], val options: PlotOptions,
                           val pointSize: Double = 3.0, colorBar: ColorBar = SingletonColorBar(Black)) extends Chart with ContinuousAxes {
  val defaultXAxisBounds: Bounds = Bounds(data.minBy(_.x).x, data.maxBy(_.x).x)
  val defaultYAxisBounds: Bounds = Bounds(data.minBy(_.y).y, data.maxBy(_.y).y)

  // Will return an EmptyDrawable if point is out-of-bounds.
  private[plot] def scatterPoint(x: Double, y: Double)(scaleX: Double, scaleY: Double): Drawable = {
    if (xAxisDescriptor.axisBounds.isInBounds(x) && yAxisDescriptor.axisBounds.isInBounds(y))
      Disc(pointSize, (x - xAxisDescriptor.axisBounds.min) * scaleX, (yAxisDescriptor.axisBounds.max - y) * scaleY)
    else EmptyDrawable()
  }

  def plottedData(extent: Extent): Drawable = {
    val scaleX: Double = extent.width / xAxisDescriptor.axisBounds.range
    val scaleY: Double = extent.height / yAxisDescriptor.axisBounds.range

    val points = (zData, colorBar) match {
      case (Some(_zData), _colorBar: ScaledColorBar) =>
        require(_zData.length == data.length, "color and point data must have same length")
        (data zip _zData).map { case (Point(x, y), z) =>
          Translate(-pointSize)(scatterPoint(x, y)(scaleX, scaleY) filled _colorBar.getColor(z))
        }
      case (_, SingletonColorBar(color)) =>
        data.map { case Point(x, y) => scatterPoint(x, y)(scaleX, scaleY) filled color }
      case (_, _) => throw new IllegalArgumentException
    }
    points.group
  }
}
