package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.{Drawable, Extent}

object Overlay {

  type OverlayData = Seq[Plot[_]]

  private def overlayPlotRenderer(plot: Plot[OverlayData], plotExtent: Extent): Drawable = {
    val paddedPlots = Plot.padPlots(Seq(plot.data), plotExtent).head
    paddedPlots.map(_.render(plotExtent)).group
  }

  def apply(plots: Plot[_]*): Plot[OverlayData] = {

    // Update bounds on subplots.
    val xbounds = Plot.combineBounds(plots.map(_.xbounds))
    val ybounds = Plot.combineBounds(plots.map(_.ybounds))
    val updatedPlots = plots.map { subplot =>
      val withX = if (subplot.xfixed) subplot else subplot.xbounds(xbounds)
      if (withX.yfixed) withX else withX.ybounds(ybounds)
    }

    Plot[OverlayData](
      data = updatedPlots,
      xbounds = xbounds,
      ybounds = ybounds,
      renderer = overlayPlotRenderer
    )
  }
}
