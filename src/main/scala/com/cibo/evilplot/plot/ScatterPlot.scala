package com.cibo.evilplot.plot

import com.cibo.evilplot.colors.Colors.{ColorBar, GradientColorBar, SingletonColorBar}
import com.cibo.evilplot.colors._
import com.cibo.evilplot.geometry._
import org.scalajs.dom.CanvasRenderingContext2D

class ScatterPlot(val extent: Extent, data: Seq[Point], zData: Option[Seq[Double]], options: PlotOptions,
                  val pointSize: Double = 3.0, colorBar: ColorBar = SingletonColorBar(Black)) extends Drawable {
  private[plot] val xAxisBounds = options.xAxisBounds.getOrElse(Bounds(data.minBy(_.x).x, data.maxBy(_.x).x))
  private[plot] val yAxisBounds = options.yAxisBounds.getOrElse(Bounds(data.minBy(_.y).y, data.maxBy(_.y).y))

  private val scaleX: Double = extent.width / xAxisBounds.range
  private val scaleY: Double = extent.height / yAxisBounds.range

  private[plot] val xAxis = new XAxis(extent, xAxisBounds.min, xAxisBounds.max, options.numXTicks.getOrElse(10))
  private val xGridLines = new VerticalGridLines(xAxis, options.xGridSpacing.getOrElse(1000), White)
  private[plot] val yAxis = new YAxis(extent, yAxisBounds.min, yAxisBounds.max, options.numYTicks.getOrElse(10))
  private val yGridLines = new HorizontalGridLines(yAxis, options.xGridSpacing.getOrElse(1000), White)

  // A complete hack. The chartArea needs to be bigger than one might think just in case we need to plot
  // a point at the axis extremes.
  private[plot] val chartArea = {
    val boundingBox = Rect(extent.width + 2 * pointSize, extent.height + 2 * pointSize) filled White
    val chartBackground = Rect(extent.width, extent.height) filled options.backgroundColor
    val _chartArea = chartBackground behind yGridLines behind (xGridLines transY extent.height)
    boundingBox behind (_chartArea transX pointSize transY pointSize)
  }

  private[plot] def scatterPoint(x: Double, y: Double): Drawable = {
    // Should this be a fatal error, or should the point just be skipped, or something else?
    if (xAxisBounds.isInBounds(x) && yAxisBounds.isInBounds(y)) Disc(pointSize, (x - xAxisBounds.min) * scaleX,
      (y - yAxisBounds.min) * scaleY) transY pointSize
    else EmptyDrawable()
  }

  private val plottedPoints = FlipY({
    val points = (zData, colorBar) match {
      case (Some(_zData), _colorBar @ GradientColorBar(_, _, _)) =>
        require(_zData.length == data.length, "color and point data must have same length")
        (data zip _zData).map { case (Point(x, y), z) => scatterPoint(x, y) filled _colorBar.getColor(z) }
      case (_, SingletonColorBar(color)) => data.map { case Point(x, y) => scatterPoint(x, y) filled color }
      case (_, _) => throw new IllegalArgumentException
    }
    points.group
  })


  private val assembledChart = yAxis transY pointSize beside
    (xAxis transX pointSize below (chartArea behind plottedPoints))

  val titledChart: Drawable = options.title match {
    case Some(_title) => assembledChart titled (_title, 20)
    case None => assembledChart
  }

  override def draw(canvas: CanvasRenderingContext2D): Unit = titledChart.draw(canvas)
}
