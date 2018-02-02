package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.{Drawable, Extent, Translate}
import com.cibo.evilplot.numeric.Bounds

object Facets {

  type FacetData = Seq[Seq[Plot[_]]]

  private def computeBounds(bounds: Seq[Bounds]): Bounds = {
    val minValue = bounds.map(_.min).min
    val maxValue = bounds.map(_.max).max
    Bounds(minValue, maxValue)
  }

  private def facetedPlotRenderer(plot: Plot[FacetData], extent: Extent): Drawable = {

    // Divide the extent evenly among subplots.
    val rows = plot.data.size
    val cols = plot.data.map(_.size).max
    val subplotExtent = Extent(extent.width / cols, extent.height / rows)

    // We need to ensure the plot area for all plots is the same size.
    // First we get the offsets of all subplots.  By selecting the largest
    // offset, we can pad all plots to start at the same location.
    val plotOffsets = plot.data.flatMap(_.map(_.plotOffset))
    val xoffset = plotOffsets.maxBy(_.x).x
    val yoffset = plotOffsets.maxBy(_.y).y

    // Update the plots with their offsets.
    val offsetPlots = plot.data.map { row =>
      row.map { subplot =>
        subplot.padTop(yoffset - subplot.plotOffset.y).padLeft(xoffset - subplot.plotOffset.x)
      }
    }

    // Now the subplots all start at the same place, so we need to ensure they all
    // end at the same place.  We do this by computing the maximum right and bottom
    // fill amounts and then padding.
    val plotAreas = plot.data.flatMap(_.map(_.plotExtent(subplotExtent)))
    val minWidth = plotAreas.minBy(_.width).width
    val minHeight = plotAreas.minBy(_.height).height
    val paddedPlots = offsetPlots.map { row =>
      row.map { subplot =>
        val pe = subplot.plotExtent(subplotExtent)
        val fillx = pe.width - minWidth
        val filly = pe.height - minHeight
        subplot.padRight(fillx).padBottom(filly)
      }
    }

    // Render the plots (offset and padded).
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
    val columnXBounds = plots.transpose.map(col => computeBounds(col.map(_.xbounds)))

    // Y bounds for each row.
    val rowYBounds = plots.map(row => computeBounds(row.map(_.ybounds)))

    // Update bounds and add padding on subplots.
    val updatedPlots = plots.zipWithIndex.map { case (row, y) =>
      row.zipWithIndex.map { case (subplot, x) =>
        subplot.xbounds(columnXBounds(x)).ybounds(rowYBounds(y))
      }
    }

    Plot[Seq[Seq[Plot[_]]]](
      data = updatedPlots,
      xbounds = computeBounds(columnXBounds),
      ybounds = computeBounds(rowYBounds),
      renderer = facetedPlotRenderer
    )
  }
}

