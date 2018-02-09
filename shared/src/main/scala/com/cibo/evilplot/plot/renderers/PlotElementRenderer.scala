package com.cibo.evilplot.plot.renderers

import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent}
import com.cibo.evilplot.plot.LegendContext

/** A renderer for an element that is plotted (such as a point or bar).
  * @tparam C Type of additional context used to identify categories.
  */
trait PlotElementRenderer[C] {
  /** Get the legend context if applicable. */
  def legendContext: Option[LegendContext[C]] = None

  /** Render a label for the category. */
  def renderLabel(context: C): Drawable = EmptyDrawable()

  /** Render a category within the extent. */
  def render(extent: Extent, context: C): Drawable
}
