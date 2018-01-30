package com.cibo.evilplot.plot

import com.cibo.evilplot.colors.{ScaledColorBar, SingletonColorBar}
import com.cibo.evilplot.geometry._
import com.cibo.evilplot.numeric.{Bounds, Point}
import com.cibo.evilplot.plotdefs.{PlotOptions, ScatterPlotDef, Trendline}

case class ScatterPlot(val chartSize: Extent, definition: ScatterPlotDef) extends Chart with ContinuousAxes {
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

  private[plot] def trendLine(trendline: Trendline,
                xBounds: Bounds,
                yBounds: Bounds): Option[Seq[Point]] = {

    def inPlotBounds(p: Point): Boolean = xBounds.isInBounds(p.x) && yBounds.isInBounds(p.y)
    // from two points, return one in the plot window or None if neither is visible.
    def theOneInBounds(a: Point, b: Point): Option[Point] =
      if (inPlotBounds(a)) Some(a) else if (inPlotBounds(b)) Some(b) else None

    val p1 = Point(trendline.solveForX(yBounds.min), yBounds.min)
    val p2 = Point(xBounds.min, trendline.valueAt(xBounds.min))

    val endPoints = theOneInBounds(p1, p2).flatMap { minPoint =>
      val p3 = Point(trendline.solveForX(yBounds.max), yBounds.max)
      val p4 = Point(xBounds.max, trendline.valueAt(xBounds.max))
      theOneInBounds(p3, p4).map(maxPoint => Seq(minPoint, maxPoint))
    }

    endPoints
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

    val line = definition.trendLine.fold(EmptyDrawable(): Drawable) { tl =>
      val endpoints = trendLine(tl, xAxisDescriptor.axisBounds, yAxisDescriptor.axisBounds)
      endpoints.fold(EmptyDrawable(): Drawable)(ep => Path(ep.map(p => transform(p)), 2))
    }

    points.group behind line
  }
}
