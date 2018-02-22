package com.cibo.evilplot.plot.renderers

import com.cibo.evilplot.colors.{Color, DefaultColors, ScaledColorBar}
import com.cibo.evilplot.geometry._
import com.cibo.evilplot.plot.{Bar, LegendContext, Plot}

trait BarRenderer extends PlotElementRenderer[Bar] {
  def render(plot: Plot, extent: Extent, category: Bar): Drawable
}

object BarRenderer {

  def stackedRenderer(
    colors: Seq[Color],
    labels: Seq[String] = Seq.empty
  ): BarRenderer = new BarRenderer {
    override def legendContext: LegendContext = LegendContext.combine {
      colors.zip(labels).map { case (color, label) =>
        val element = Rect(Text.defaultSize, Text.defaultSize).filled(color)
        LegendContext.single(element, label)
      }
    }
    def render(plot: Plot, extent: Extent, bar: Bar): Drawable = {
      val scale = if (bar.height == 0) 0.0 else extent.height / bar.height
      bar.values.zipWithIndex.map { case (value, stackIndex) =>
        val height = value * scale
        val width = extent.width
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
    override def legendContext: LegendContext = LegendContext.fromColorBar(colorBar)
    def render(plot: Plot, barExtent: Extent, bar: Bar): Drawable = {
      val color = colorBar.getColor(bar.height)
      Rect(barExtent.width, barExtent.height).filled(color)
    }
  }

}
