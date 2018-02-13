package com.cibo.evilplot.plot.components

import com.cibo.evilplot.geometry._
import com.cibo.evilplot.plot.Plot

/** A component that is aligned with the data of a plot. */
trait PlotComponent {

  // The position of this component.
  val position: Position

  // Determines if this component is repeated in facets.
  // For example, axes and backgrounds are repeated.
  val repeated: Boolean = false

  // Get the minimum size of this component.
  def size(plot: Plot): Extent = Extent(0, 0)

  // Render the component.
  def render(plot: Plot, extent: Extent): Drawable
}
