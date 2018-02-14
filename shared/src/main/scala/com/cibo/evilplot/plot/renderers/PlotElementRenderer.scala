package com.cibo.evilplot.plot.renderers

import com.cibo.evilplot.geometry.{Drawable, Extent}
import com.cibo.evilplot.plot.{LegendContext, Plot}

/** A renderer for an element that is plotted (such as a point or bar).
  * @tparam C Type of context used to identify categories.
  */
trait PlotElementRenderer[C] {
  /** Get the legend context if applicable. */
  def legendContext: Option[LegendContext] = None

  /** Render a category within the extent. */
  def render(plot: Plot, extent: Extent, context: C): Drawable
}
