package com.cibo.evilplot.plot.components

import com.cibo.evilplot.geometry.{Drawable, Extent}
import com.cibo.evilplot.plot.Plot
import com.cibo.evilplot.plot.aesthetics.Theme

/** A component that is aligned with the data of a plot. */
trait FacetedPlotComponent {

  /** The position of this component. */
  val position: Position

  /** Determine if this component is repeated in facets.
    * For example, axes and backgrounds are repeated.
    */
  val repeated: Boolean = false

  /** Get the minimum size of this component. */
  def size(plot: Plot): Extent = Extent(0, 0)

  /** Render the component for a particular facet.
    * @param plot The plot or subplot if a facet.
    * @param extent The extent this component gets.
    * @param row The facet row (or 0).
    * @param column The facet column (or 0).
    */
  def render(plot: Plot, extent: Extent, row: Int, column: Int)(implicit theme: Theme): Drawable
}
