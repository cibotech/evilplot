package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.{Drawable, Extent}
import com.cibo.evilplot.numeric.Bounds
import com.cibo.evilplot.plot.Overlay.OverlayData
import com.cibo.evilplot.plot.renderers.PlotRenderer

object Overlay {

  type OverlayData = Seq[Plot[_]]

  // Update subplots to have the specified bounds (if not already fixed).
  private def updateSubplotBounds(
    subplots: Seq[Plot[_]],
    xbounds: Bounds,
    ybounds: Bounds
  ): Seq[Plot[_]] = {
    subplots.map { subplot =>
      (subplot.xfixed, subplot.yfixed) match {
        case (true, true)   => subplot
        case (false, true)  => subplot.updateBounds(xbounds, subplot.ybounds)
        case (true, false)  => subplot.updateBounds(subplot.xbounds, ybounds)
        case (false, false) => subplot.updateBounds(xbounds, ybounds)
      }
    }
  }

  // Update subplots to have the same transform (if not fixed).
  private def getTransformedSubplots(plot: Plot[OverlayData]): OverlayData = {
    plot.data.map { subplot =>
      val withX = if (subplot.xfixed) subplot else subplot.setXTransform(plot.xtransform, fixed = false)
      if (withX.yfixed) withX else withX.setYTransform(plot.ytransform, fixed = false)
    }
  }

  private case object OverlayPlotRenderer extends PlotRenderer[OverlayData] {
    def render(plot: Plot[OverlayData], plotExtent: Extent): Drawable = {
      val updatedPlots = updateSubplotBounds(
        subplots = Plot.padPlots(Seq(getTransformedSubplots(plot)), plotExtent, 0, 0).head,
        xbounds = plot.xbounds,
        ybounds = plot.ybounds
      )
      updatedPlots.map(_.render(plotExtent)).group
    }
  }

  def apply(plots: Plot[_]*): Plot[OverlayData] = {
    require(plots.nonEmpty, "must have at least one plot for an overlay")

    // Update bounds on subplots.
    val xbounds = Plot.combineBounds(plots.map(_.xbounds))
    val ybounds = Plot.combineBounds(plots.map(_.ybounds))
    val updatedPlots = updateSubplotBounds(plots, xbounds, ybounds)

    Plot[OverlayData](
      data = updatedPlots,
      xbounds = xbounds,
      ybounds = ybounds,
      renderer = OverlayPlotRenderer,
      legendContext = plots.flatMap(_.legendContext).headOption
    )
  }
}

trait OverlayImplicits[T] {
  protected val plot: Plot[T]

  def overlay[U](plot2: Plot[U]): Plot[OverlayData] = Overlay(plot, plot2)
}
