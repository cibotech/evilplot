package com.cibo.evilplot.plot.renderers

import com.cibo.evilplot.colors.{Color, DefaultColors, ScaledColorBar}
import com.cibo.evilplot.geometry._
import com.cibo.evilplot.plot.{Bar, Plot}

trait BarRenderer[T] extends PlotElementRenderer[T, Bar] {
  def render(plot: Plot[T], extent: Extent, category: Bar): Drawable
}

object BarRenderer {

  def stackedRenderer[T](
    colors: Seq[Color]
  ): BarRenderer[T] = new BarRenderer[T] {
    def render(plot: Plot[T], extent: Extent, bar: Bar): Drawable = {
      val scale = if (bar.height == 0) 0.0 else extent.height / bar.height
      bar.values.zipWithIndex.map { case (value, stackIndex) =>
        val height = value * scale
        val width = extent.width
        Rect(width, height).filled(colors(stackIndex))
      }.reduce(_ below _)
    }
  }

  def default[T](
    color: Color = DefaultColors.barColor
  ): BarRenderer[T] = stackedRenderer(Seq(color))

  def temperature[T](
    colors: Seq[Color] = Color.stream
  )(
    bars: Seq[Seq[Bar]]
  ): BarRenderer[T] = {
    val minValue = bars.flatten.minBy(_.height).height
    val maxValue = bars.flatten.maxBy(_.height).height
    val colorCount = bars.flatten.map(_.height).distinct.size
    temperature(ScaledColorBar(colors.take(colorCount), minValue, maxValue))
  }

  def temperature[T](
    colorBar: ScaledColorBar
  ): BarRenderer[T] = new BarRenderer[T] {
    def render(plot: Plot[T], barExtent: Extent, bar: Bar): Drawable = {
      val color = colorBar.getColor(bar.height)
      Rect(barExtent.width, barExtent.height).filled(color)
    }
  }

}
