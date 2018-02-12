package com.cibo.evilplot.plot.renderers

import com.cibo.evilplot.geometry.{Drawable, Extent}
import com.cibo.evilplot.plot.{LegendContext, Plot}

/** A renderer for an element that is plotted (such as a point or bar).
  * @tparam T Type of data to render.
  * @tparam C Type of context used to identify categories.
  */
trait PlotElementRenderer[T, C] {
  /** Get the legend context if applicable. */
  def legendContext(data: T): Option[LegendContext[C]] = None

  /** Render a category within the extent. */
  def render(extent: Extent, data: T, context: C): Drawable
}
