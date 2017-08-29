package com.cibo.evilplot.plot

import com.cibo.evilplot.Utils
import com.cibo.evilplot.colors.Colors.{ColorBar, GradientColorBar, SingletonColorBar}
import com.cibo.evilplot.colors._
import com.cibo.evilplot.geometry._
import com.cibo.evilplot.layout.ChartLayout
import com.cibo.evilplot.numeric.AxisDescriptor
import org.scalajs.dom.CanvasRenderingContext2D

case class ScatterPlotData(data: Seq[Point], zData: Option[Seq[Double]], pointSize: Double = 2.25,
                           colorBar: ColorBar = SingletonColorBar(Black)) extends PlotData {
  override def xBounds: Option[Bounds] = Some(Bounds(data.minBy(_.x).x, data.maxBy(_.x).x))
  override def yBounds: Option[Bounds] = Some(Bounds(data.minBy(_.y).y, data.maxBy(_.y).y))
  override def createPlot(extent: Extent, options: PlotOptions): Drawable = {
    new ScatterPlot(extent, data, zData, options, pointSize = pointSize, colorBar = colorBar)
  }
}

class ScatterPlot(val extent: Extent, data: Seq[Point], zData: Option[Seq[Double]], options: PlotOptions,
                  val pointSize: Double = 3.0, colorBar: ColorBar = SingletonColorBar(Black)) extends Drawable {

  private[plot] val xAxisBounds = options.xAxisBounds.getOrElse(Bounds(data.minBy(_.x).x, data.maxBy(_.x).x))
  private[plot] val yAxisBounds = options.yAxisBounds.getOrElse(Bounds(data.minBy(_.y).y, data.maxBy(_.y).y))
  private[plot] val xAxisDescriptor = AxisDescriptor(xAxisBounds, options.numXTicks.getOrElse(10))
  private[plot] val yAxisDescriptor = AxisDescriptor(yAxisBounds, options.numYTicks.getOrElse(10))
  // Will return an EmptyDrawable if point is out-of-bounds.
  private[plot] def scatterPoint(x: Double, y: Double)(scaleX: Double, scaleY: Double): Drawable = {
    if (xAxisDescriptor.axisBounds.isInBounds(x) && yAxisDescriptor.axisBounds.isInBounds(y))
      Disc(pointSize, (x - xAxisDescriptor.axisBounds.min) * scaleX, (yAxisDescriptor.axisBounds.max - y) * scaleY)
    else EmptyDrawable()
  }
  private val _drawable: Drawable = {
    val xAxis: DrawableLater = ContinuousChartDistributable.XAxis(xAxisDescriptor, label = options.xAxisLabel,
      drawTicks = options.drawXAxis)
    val yAxis: DrawableLater = ContinuousChartDistributable.YAxis(yAxisDescriptor, label = options.yAxisLabel,
      drawTicks = options.drawYAxis)
    val topLabel = Utils.maybeDrawableLater(options.topLabel, (text: String) => Label(text, textSize = Some(20)))
    val rightLabel = Utils.maybeDrawableLater(options.rightLabel,
      (text: String) => Label(text, textSize = Some(20), rotate = 90))

    def chartArea(extent: Extent): Drawable = {
      val scaleX: Double = extent.width / xAxisDescriptor.axisBounds.range
      val scaleY: Double = extent.height / yAxisDescriptor.axisBounds.range
      val chartBackground = Rect(extent.width, extent.height) filled options.backgroundColor
      val xGridLines = ContinuousChartDistributable.
        VerticalGridLines(xAxisDescriptor, options.xGridSpacing.getOrElse(1000), color = options.gridColor)(extent)
      val yGridLines = ContinuousChartDistributable.
        HorizontalGridLines(yAxisDescriptor, options.yGridSpacing.getOrElse(1000), color = options.gridColor)(extent)
      val _chartArea = chartBackground behind yGridLines behind xGridLines

      val plottedPoints = {
        val points = (zData, colorBar) match {
          case (Some(_zData), _colorBar@GradientColorBar(_, _, _, _, _)) =>
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

      (_chartArea transX pointSize transY pointSize behind plottedPoints) transX -pointSize transY -pointSize
    }

    new ChartLayout(extent, preferredSizeOfCenter = extent * 0.85, center = new DrawableLaterMaker(chartArea),
      bottom = xAxis, left = yAxis, top = topLabel, right = rightLabel)
  }

  override def draw(canvas: CanvasRenderingContext2D): Unit = _drawable.draw(canvas)
}
