package com.cibo.evilplot.plot.renderers

import com.cibo.evilplot.colors.Color
import com.cibo.evilplot.geometry._
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.{Bar, Plot}

trait BarRenderer extends PlotElementRenderer[Bar] {
  def render(plot: Plot, extent: Extent, category: Bar): Drawable
}

object BarRenderer {

  /** Default bar renderer. */
  def default(
    color: Option[Color] = None
  )(implicit theme: Theme): BarRenderer = new BarRenderer {
    def render(plot: Plot, extent: Extent, bar: Bar): Drawable = {
      Rect(extent.width, extent.height).filled(color.getOrElse(theme.colors.bar))
    }
  }

  /** Create a bar renderer to render a clustered bar chart. */
  def clustered(): BarRenderer = new BarRenderer {
    def render(plot: Plot, extent: Extent, bar: Bar): Drawable = {
      Rect(extent.width, extent.height).filled(bar.getColor(0))
    }
  }

  /** Create a bar renderer to render a stacked bar chart. */
  def stacked(): BarRenderer = new BarRenderer {
    def render(plot: Plot, extent: Extent, bar: Bar): Drawable = {
      val scale = if (bar.height == 0) 0.0 else extent.height / bar.height
      bar.values.zipWithIndex.map { case (value, stackIndex) =>
        val height = value * scale
        val width = extent.width
        Rect(width, height).filled(bar.getColor(stackIndex))
      }.reduce(_ below _)
    }
  }
}
