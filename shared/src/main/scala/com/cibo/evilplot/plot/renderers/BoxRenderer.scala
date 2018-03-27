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

package com.cibo.evilplot.plot.renderers

import com.cibo.evilplot.geometry.{Align, BorderRect, Drawable, Extent, Line, StrokeStyle}
import com.cibo.evilplot.numeric.BoxPlotSummaryStatistics
import com.cibo.evilplot.plot.Plot
import com.cibo.evilplot.plot.aesthetics.Theme

trait BoxRenderer extends PlotElementRenderer[BoxPlotSummaryStatistics] {
  def render(plot: Plot, extent: Extent, summary: BoxPlotSummaryStatistics): Drawable
}

object BoxRenderer {
  def default()(implicit theme: Theme): BoxRenderer = new BoxRenderer {
    def render(
      plot: Plot,
      extent: Extent,
      summary: BoxPlotSummaryStatistics
    ): Drawable = {
      val scale = extent.height / (summary.upperWhisker - summary.lowerWhisker)
      val topWhisker = summary.upperWhisker - summary.upperQuantile
      val uppperToMiddle = summary.upperQuantile - summary.middleQuantile
      val middleToLower = summary.middleQuantile - summary.lowerQuantile
      val bottomWhisker = summary.lowerQuantile - summary.lowerWhisker

      Align.center(
        StrokeStyle(Line(scale * topWhisker, theme.elements.strokeWidth), theme.colors.path).rotated(90),
        BorderRect.filled(extent.width, scale * uppperToMiddle).colored(theme.colors.path).filled(theme.colors.fill),
        BorderRect.filled(extent.width, scale * middleToLower).colored(theme.colors.path).filled(theme.colors.fill),
        StrokeStyle(Line(scale * bottomWhisker, theme.elements.strokeWidth), theme.colors.path).rotated(90)
      ).reduce(_ above _)
    }
  }
}
