package com.cibo.evilplot.plot

import com.cibo.evilplot.colors.{Color, DefaultColors}
import com.cibo.evilplot.geometry._

final case class Bar(values: Seq[Double]) {
  lazy val height: Double = values.sum
}

object Bar {
  def apply(value: Double): Bar = Bar(Seq(value))
}

trait BarRenderer {
  def render(bar: Bar, barExtent: Extent, index: Int): Drawable
}

object BarRenderer {

  val defaultBarSpacing: Double = 1.0

  def stackedRenderer(
    colors: Seq[Color],
    grouping: Int => Int = _ => 0,
    spacing: Double = defaultBarSpacing
  ): BarRenderer = new BarRenderer {
    def render(bar: Bar, barExtent: Extent, index: Int): Drawable = {
      val scale = barExtent.height / bar.height
      bar.values.zipWithIndex.map { case (value, stackIndex) =>
        val height = value * scale
        val width = math.max(barExtent.width - spacing, 1.0)
        Rect(width, height).colored(colors(stackIndex))
      }.reduce(_ below _)
    }
  }

  def default(
    color: Color = DefaultColors.barColor,
    spacing: Double = defaultBarSpacing
  ): BarRenderer = stackedRenderer(Seq(color), spacing = defaultBarSpacing)

}
