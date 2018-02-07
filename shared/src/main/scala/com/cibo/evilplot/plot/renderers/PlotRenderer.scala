package com.cibo.evilplot.plot.renderers

import com.cibo.evilplot.geometry.{Drawable, Extent}
import com.cibo.evilplot.plot.Plot

/** Renderer for the plot area. */
trait PlotRenderer[T] {
  def render(plot: Plot[T], plotExtent: Extent): Drawable
}
