package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent}
import com.cibo.evilplot.numeric.Bounds
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.renderers.PlotRenderer

object EmptyPlot {
  // Renderer to do nothing.
  private[evilplot] case object EmptyPlotRenderer extends PlotRenderer {
    def render(plot: Plot, plotExtent: Extent)(
        implicit theme: Theme): Drawable =
      EmptyDrawable().resize(plotExtent)
  }
  def apply(xbounds: Option[Bounds] = None,
            ybounds: Option[Bounds] = None): Plot =
    Plot(xbounds.getOrElse(Bounds(0, 1)),
         ybounds.getOrElse(Bounds(0, 1)),
         EmptyPlotRenderer)
}
