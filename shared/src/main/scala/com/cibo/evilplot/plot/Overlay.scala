package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.{Drawable, Extent}
import com.cibo.evilplot.plot.renderers.PlotRenderer

object Overlay {

  type OverlayData = Seq[Plot[_]]

  private case object OverlayPlotRenderer extends PlotRenderer[OverlayData] {
    def render(plot: Plot[OverlayData], plotExtent: Extent): Drawable = {
      val paddedPlots = Plot.padPlots(Seq(plot.data), plotExtent, 0, 0).head.map { subplot =>
        val withX = if (subplot.xfixed) subplot else subplot.setXTransform(plot.xtransform, fixed = false)
        if (withX.yfixed) withX else withX.setYTransform(plot.ytransform, fixed = false)
      }
      paddedPlots.map(_.render(plotExtent)).group
    }
  }

  def apply(plots: Plot[_]*): Plot[OverlayData] = {
    require(plots.nonEmpty, "must have at least one plot for an overlay")

    // Update bounds on subplots.
    val xbounds = Plot.combineBounds(plots.map(_.xbounds))
    val ybounds = Plot.combineBounds(plots.map(_.ybounds))
    val updatedPlots = plots.map { subplot =>
      (subplot.xfixed, subplot.yfixed) match {
        case (true, true)   => subplot
        case (false, true)  => subplot.updateBounds(xbounds, subplot.ybounds)
        case (true, false)  => subplot.updateBounds(subplot.xbounds, ybounds)
        case (false, false) => subplot.updateBounds(xbounds, ybounds)
      }
    }

    Plot[OverlayData](
      data = updatedPlots,
      xbounds = xbounds,
      ybounds = ybounds,
      renderer = OverlayPlotRenderer,
      legendContext = plots.flatMap(_.legendContext).headOption
    )
  }
}
