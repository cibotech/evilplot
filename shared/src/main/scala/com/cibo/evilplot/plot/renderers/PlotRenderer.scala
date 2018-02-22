package com.cibo.evilplot.plot.renderers

import com.cibo.evilplot.geometry.{Drawable, Extent}
import com.cibo.evilplot.plot.{LegendContext, Plot}

/** Renderer for the plot area. */
trait PlotRenderer {
  def legendContext: LegendContext = LegendContext.empty
  def render(plot: Plot, plotExtent: Extent): Drawable
}
