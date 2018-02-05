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

  def stackedRenderer(
    colors: Seq[Color],
    grouping: Int => Int = _ => 0
  ): BarRenderer = new BarRenderer {
    def render(bar: Bar, barExtent: Extent, index: Int): Drawable = {
      val scale = barExtent.height / bar.height
      bar.values.zipWithIndex.map { case (value, stackIndex) =>
        val height = value * scale
        Rect(barExtent.width, height).colored(colors(stackIndex))
      }.reduce(_ below _)
    }
  }

  def default(color: Color = DefaultColors.barColor): BarRenderer = stackedRenderer(Seq(color))

}
