/*
 * Copyright (c) 2018, CiBO Technologies, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.{Drawable, Extent}
import com.cibo.evilplot.numeric.Bounds
import com.cibo.evilplot.plot.aesthetics.Theme
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
      val withX =
        if (subplot.xfixed) subplot else subplot.setXTransform(plot.xtransform, fixed = false)
      if (withX.yfixed) withX else withX.setYTransform(plot.ytransform, fixed = false)
    }
  }

  final case class OverlayPlotRenderer(subplots: Seq[Plot]) extends PlotRenderer {
    override def legendContext: LegendContext =
      LegendContext.combine(subplots.map(_.renderer.legendContext))
    def render(plot: Plot, plotExtent: Extent)(implicit theme: Theme): Drawable = {
      val updatedPlots = updateSubplotBounds(
        subplots = Plot.padPlots(Seq(getTransformedSubplots(plot, subplots)), plotExtent, 0, 0).head,
        xbounds = plot.xbounds,
        ybounds = plot.ybounds
      )
      updatedPlots.map(_.render(plotExtent)).group
    }
  }

  /** Overlay one or more plots. */
  def apply(plots: Plot*): Plot = {
    require(plots.nonEmpty, "must have at least one plot for an overlay")

    // Update bounds on subplots.
    val xbounds = Plot.combineBounds(plots.map(_.xbounds))
    val ybounds = Plot.combineBounds(plots.map(_.ybounds))
    val updatedPlots = updateSubplotBounds(plots, xbounds, ybounds)

    Plot(
      xbounds = xbounds,
      ybounds = ybounds,
      renderer = OverlayPlotRenderer(updatedPlots)
    )
  }

  /** Overlay a sequence of plots. */
  def fromSeq(plots: Seq[Plot]): Plot = apply(plots: _*)
}

trait OverlayImplicits {
  protected val plot: Plot

  def overlay(plot2: Plot): Plot = Overlay(plot, plot2)
}
