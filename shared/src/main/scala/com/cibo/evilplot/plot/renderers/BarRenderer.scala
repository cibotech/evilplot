package com.cibo.evilplot.plot.renderers

import com.cibo.evilplot.colors.{Color, DefaultColors, ScaledColorBar}
import com.cibo.evilplot.geometry._
import com.cibo.evilplot.plot.Bar

trait BarRenderer {
  def render(bar: Bar, barExtent: Extent, index: Int): Drawable
}

object BarRenderer {

  def stackedRenderer(
    colors: Seq[Color]
  ): BarRenderer = new BarRenderer {
    def render(bar: Bar, barExtent: Extent, index: Int): Drawable = {
      val scale = if (bar.height == 0) 0.0 else barExtent.height / bar.height
      bar.values.zipWithIndex.map { case (value, stackIndex) =>
        val height = value * scale
        val width = barExtent.width
        Rect(width, height).filled(colors(stackIndex))
      }.reduce(_ below _)
    }
  }

  def default(
    color: Color = DefaultColors.barColor
  ): BarRenderer = stackedRenderer(Seq(color))

  def temperature(
    colors: Seq[Color] = Color.stream
  )(
    bars: Seq[Seq[Bar]]
  ): BarRenderer = {
    val minValue = bars.flatten.minBy(_.height).height
    val maxValue = bars.flatten.maxBy(_.height).height
    val colorCount = bars.flatten.map(_.height).distinct.size
    temperature(ScaledColorBar(colors.take(colorCount), minValue, maxValue))
  }

  def temperature(
    colorBar: ScaledColorBar
  ): BarRenderer = new BarRenderer {
    def render(bar: Bar, barExtent: Extent, index: Int): Drawable = {
      val color = colorBar.getColor(bar.height)
      Rect(barExtent.width, barExtent.height).filled(color)
    }
  }

}
