package com.cibo.evilplot.plot.components

import com.cibo.evilplot.colors.{Color, DefaultColors}
import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent, Line, Path}
import com.cibo.evilplot.numeric.Point
import com.cibo.evilplot.plot.Plot

sealed trait PlotLine extends PlotComponent {
  val position: Position = Position.Overlay
}

case class HorizontalPlotLine(y: Double, thickness: Double, color: Color) extends PlotLine {
  def render[T](plot: Plot[T], extent: Extent): Drawable = {
    val offset = plot.ytransform(plot, extent)(y)
    Line(extent.width, thickness).colored(color).translate(y = offset)
  }
}

case class VerticalPlotLine(x: Double, thickness: Double, color: Color) extends PlotLine {
  def render[T](plot: Plot[T], extent: Extent): Drawable = {
    val offset = plot.xtransform(plot, extent)(x)
    Line(extent.height, thickness).colored(color).rotated(90).translate(x = offset)
  }
}

case class TrendPlotLine(slope: Double, intercept: Double, color: Color, thickness: Double) extends PlotLine {
  private def solveForX(y: Double): Double = (y - intercept) / slope
  private def valueAt(x: Double): Double = x * slope + intercept

  private def points(plot: Plot[_]): Option[Seq[Point]] = {

    // from two points, return one in the plot window or None if neither is visible.
    def pointInBounds(a: Point, b: Point): Option[Point] =
      if (plot.inBounds(a)) Some(a) else Some(b).filter(plot.inBounds)

    val p1 = Point(solveForX(plot.ybounds.min), plot.ybounds.min)
    val p2 = Point(plot.xbounds.min, valueAt(plot.xbounds.min))
    val p3 = Point(solveForX(plot.ybounds.max), plot.ybounds.max)
    val p4 = Point(plot.xbounds.max, valueAt(plot.xbounds.max))
    val candidates = Seq(p1, p2, p3, p4).filter(plot.inBounds)
    if (candidates.length >= 2) {
      Some(Seq(candidates.minBy(_.x), candidates.maxBy(_.x)))
    } else {
      None
    }
  }

  def render[T](plot: Plot[T], extent: Extent): Drawable = {
    val xtransform = plot.xtransform(plot, extent)
    val ytransform = plot.ytransform(plot, extent)
    points(plot).map { ps =>
      val transformedPoints = ps.map(p => Point(xtransform(p.x), ytransform(p.y)))
      Path(transformedPoints, thickness).colored(color)
    }.getOrElse(EmptyDrawable())
  }
}

trait PlotLineImplicits[T] {
  protected val plot: Plot[T]

  val defaultColor: Color = DefaultColors.barColor
  val defaultThickness: Double = 2.0

  def hline(
    y: Double,
    color: Color = defaultColor,
    thickness: Double = defaultThickness
  ): Plot[T] = plot :+ HorizontalPlotLine(y, thickness, color)

  def vline(
    x: Double,
    color: Color = defaultColor,
    thickness: Double = defaultThickness
  ): Plot[T] = plot :+ VerticalPlotLine(x, thickness, color)

  def trend(
    slope: Double,
    intercept: Double,
    color: Color = defaultColor,
    thickness: Double = defaultThickness
  ): Plot[T] = plot :+ TrendPlotLine(slope, intercept, color, thickness)
}
