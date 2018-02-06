package com.cibo.evilplot.plot.renderers

import com.cibo.evilplot.colors.{Color, DefaultColors}
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
      val scale = barExtent.height / bar.height
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

}
