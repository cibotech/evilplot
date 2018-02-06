package com.cibo.evilplot.plot.components

import com.cibo.evilplot.colors.{Color, DefaultColors}
import com.cibo.evilplot.geometry.{BorderRect, Drawable, Extent, Line, Rect}
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
  def render[T](plot: Plot[T], extent: Extent): Drawable = {
    ???
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
