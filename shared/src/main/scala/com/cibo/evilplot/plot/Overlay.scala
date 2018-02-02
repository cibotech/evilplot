package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.{Drawable, Extent}

object Overlay {

  type OverlayData = Seq[Plot[_]]

  private def overlayPlotRenderer(plot: Plot[OverlayData], extent: Extent): Drawable = {
    val paddedPlots = Plot.padPlots(Seq(plot.data), extent).head
    paddedPlots.map(_.render(extent)).group
  }

  def apply(plots: Seq[Plot[_]]): Plot[OverlayData] = {

    // Update bounds on subplots.
    val xbounds = Plot.combineBounds(plots.map(_.xbounds))
    val ybounds = Plot.combineBounds(plots.map(_.ybounds))
    val updatedPlots = plots.map(_.xbounds(xbounds).ybounds(ybounds))

    Plot[OverlayData](
      data = updatedPlots,
      xbounds = xbounds,
      ybounds = ybounds,
      renderer = overlayPlotRenderer
    )
  }
}
