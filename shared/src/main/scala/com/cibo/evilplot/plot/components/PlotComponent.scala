package com.cibo.evilplot.plot.components

import com.cibo.evilplot.geometry._
import com.cibo.evilplot.plot.Plot
import com.cibo.evilplot.plot.aesthetics.Theme

/** A component that is aligned with the data of a plot (used when all facets are treated identically). */
trait PlotComponent extends FacetedPlotComponent {

  // Render the component (assumes all facets are handled the same way).
  def render(plot: Plot, extent: Extent)(implicit theme: Theme): Drawable

  // Render the component for a particular facet.
  // This this calls the implementation that ignores facet information.
  final def render(plot: Plot, extent: Extent, row: Int, column: Int)(implicit theme: Theme): Drawable =
    render(plot, extent)
}
