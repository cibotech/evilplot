package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.{Drawable, Extent, Translate}

object Facets {

  type FacetData = Seq[Seq[Plot[_]]]

  private def facetedPlotRenderer(plot: Plot[FacetData], extent: Extent): Drawable = {
    // Divide the extent evenly among subplots.
    val rows = plot.data.size
    val cols = plot.data.map(_.size).max
    val subplotExtent = Extent(extent.width / cols, extent.height / rows)

    // Make sure all subplots have the same size plot area.
    val paddedPlots = Plot.padPlots(plot.data, subplotExtent)

    // Render the plots.
    paddedPlots.zipWithIndex.map { case (row, yIndex) =>
      val y = yIndex * subplotExtent.height
      row.zipWithIndex.map { case (subplot, xIndex) =>
        val x = xIndex * subplotExtent.width
        Translate(subplot.render(subplotExtent), x = x, y = y)
      }.group
    }.group
  }

  def apply(plots: Seq[Seq[Plot[_]]]): Plot[FacetData] = {

    // X bounds for each column.
    val columnXBounds = plots.transpose.map(col => Plot.combineBounds(col.map(_.xbounds)))

    // Y bounds for each row.
    val rowYBounds = plots.map(row => Plot.combineBounds(row.map(_.ybounds)))

    // Update bounds on subplots.
    val updatedPlots = plots.zipWithIndex.map { case (row, y) =>
      row.zipWithIndex.map { case (subplot, x) =>
        subplot.xbounds(columnXBounds(x)).ybounds(rowYBounds(y))
      }
    }

    Plot[FacetData](
      data = updatedPlots,
      xbounds = Plot.combineBounds(columnXBounds),
      ybounds = Plot.combineBounds(rowYBounds),
      renderer = facetedPlotRenderer
    )
  }
}

