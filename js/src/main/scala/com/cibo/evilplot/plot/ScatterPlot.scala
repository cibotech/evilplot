package com.cibo.evilplot.plot

import com.cibo.evilplot.colors.Colors.{ScaledColorBar, SingletonColorBar}
import com.cibo.evilplot.geometry._
import com.cibo.evilplot.numeric.{Bounds, Point}
import com.cibo.evilplot.plotdefs.{PlotOptions, ScatterPlotDef, Trendline}

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

    val transform: AffineTransform = AffineTransform(
      shiftX = -xAxisDescriptor.axisBounds.min,
      shiftY = yAxisDescriptor.axisBounds.max, scaleY = -1).scale(x = scaleX, y = scaleY)

    val points = (definition.zData, definition.colorBar) match {
      case (Some(_zData), _colorBar: ScaledColorBar) =>
        require(_zData.length == data.length, "color and point data must have same length")
        (data zip _zData).map { case (Point(x, y), z) =>
          Disc(definition.pointSize, Point.tupled(transform(x, y))) filled _colorBar.getColor(z)
        }
      case (_, SingletonColorBar(color)) =>
        data.map { case Point(x, y) => Disc(definition.pointSize, Point.tupled(transform(x, y))) filled color }
      case (_, _) => throw new IllegalArgumentException
    }


    val trendLine: Drawable = definition.trendLine.map { tl =>
        val minXtoPlot = {
          val xAtMinY = tl.solveForX(yAxisDescriptor.axisBounds.min)
          if (xAtMinY < xAxisDescriptor.axisBounds.min) xAxisDescriptor.axisBounds.min
          else xAtMinY
        }

        val maxXToPlot = {
          val xAtMaxY = tl.solveForX(yAxisDescriptor.axisBounds.max)
          if (xAtMaxY > xAxisDescriptor.axisBounds.max) xAxisDescriptor.axisBounds.max
          else xAtMaxY
        }

//        println(minXtoPlot, maxXToPlot)
        Path(Seq(
        Point.tupled(transform(minXtoPlot, tl.valueAt(minXtoPlot))),
        Point.tupled(transform(maxXToPlot, tl.valueAt(maxXToPlot)))
      ), 2)
    }.getOrElse(EmptyDrawable())

    points.group behind trendLine
  }
}
