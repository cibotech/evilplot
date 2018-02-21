package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.{Drawable, Extent}
import com.cibo.evilplot.numeric.Bounds
import com.cibo.evilplot.plot.renderers.PlotRenderer

object Overlay {

  // Update subplots to have the specified bounds (if not already fixed).
  private def updateSubplotBounds(
    subplots: Seq[Plot],
    xbounds: Bounds,
    ybounds: Bounds
  ): Seq[Plot] = {
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
  private def getTransformedSubplots(plot: Plot, subplots: Seq[Plot]): Seq[Plot] = {
    subplots.map { subplot =>
      val withX = if (subplot.xfixed) subplot else subplot.setXTransform(plot.xtransform, fixed = false)
      if (withX.yfixed) withX else withX.setYTransform(plot.ytransform, fixed = false)
    }
  }

  private case class OverlayPlotRenderer(subplots: Seq[Plot]) extends PlotRenderer {
    def render(plot: Plot, plotExtent: Extent): Drawable = {
      val updatedPlots = updateSubplotBounds(
        subplots = Plot.padPlots(Seq(getTransformedSubplots(plot, subplots)), plotExtent, 0, 0).head,
        xbounds = plot.xbounds,
        ybounds = plot.ybounds
      )
      updatedPlots.map(_.render(plotExtent)).group
    }
  }

  def apply(plots: Plot*): Plot = {
    require(plots.nonEmpty, "must have at least one plot for an overlay")

    // Update bounds on subplots.
    val xbounds = Plot.combineBounds(plots.map(_.xbounds))
    val ybounds = Plot.combineBounds(plots.map(_.ybounds))
    val updatedPlots = updateSubplotBounds(plots, xbounds, ybounds)

    Plot(
      xbounds = xbounds,
      ybounds = ybounds,
      renderer = OverlayPlotRenderer(updatedPlots),
      legendContext = plots.map(_.legendContext).reduce(_.combine(_))
    )
  }
}

trait OverlayImplicits {
  protected val plot: Plot

  def overlay(plot2: Plot): Plot = Overlay(plot, plot2)
}
