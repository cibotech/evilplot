package com.cibo.evilplot.plot.renderers

import com.cibo.evilplot.geometry.{Drawable, Extent}
import com.cibo.evilplot.plot.Plot

trait PlotRenderer[T] {
  def render(plot: Plot[T], plotExtent: Extent): Drawable
}
