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

object MixedBoundsOverlay {

  final case class MixedBoundsOverlayPlotRenderer(subplots: Seq[Plot]) extends PlotRenderer {
    override def legendContext: LegendContext =
      LegendContext.combine(subplots.map(_.renderer.legendContext))
    def render(plot: Plot, plotExtent: Extent)(implicit theme: Theme): Drawable =
      Plot.padPlots(Seq(subplots), plotExtent, 0, 0).head.map(_.render(plotExtent)(theme)).group
  }

  /** Overlay plots without updating bounds or transforms for individual plots.
    * @param xBounds The X bounds to set for the main overlay plot (for e.g., axes)
    * @param yBounds The X bounds to set for the main overlay plot (for e.g., axes)
    * @param plots   The plots to overlay.
    */
  def apply(xBounds: Bounds, yBounds: Bounds, plots: Plot*): Plot = {
    require(plots.nonEmpty, "must have at least one plot for an overlay")
    Plot(xbounds = xBounds, ybounds = yBounds, renderer = MixedBoundsOverlayPlotRenderer(plots))
  }

  /** Overlay plots without updating bounds or transforms for individual plots.
    * @param mainPlot The plot which will set the bounds for the overlay (for e.g., axes)
    * @param plots    The remaining plots to overlay.
    */
  def apply(mainPlot: Plot, plots: Plot*): Plot = {
    this(mainPlot.xbounds, mainPlot.ybounds, mainPlot +: plots: _*)
  }

  /** Overlay a sequence of plots without updating bounds or transforms for individual plots.
    * @param xBounds The X bounds to set for the main overlay plot (for e.g., axes)
    * @param yBounds The X bounds to set for the main overlay plot (for e.g., axes)
    * @param plots   The plots to overlay.
    */
  def fromSeq(xBounds: Bounds, yBounds: Bounds, plots: Seq[Plot]): Plot = apply(xBounds, yBounds, plots: _*)

  /** Overlay a sequence of plots without updating bounds or transforms for individual plots.
    * @param mainPlot The plot which will set the bounds for the overlay (for e.g., axes)
    * @param plots    The remaining plots to overlay.
    */
  def fromSeq(mainPlot: Plot, plots: Seq[Plot]): Plot = apply(mainPlot, plots: _*)

}
