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

import com.cibo.evilplot.colors.Color
import com.cibo.evilplot.geometry.{Drawable, Extent, Rect}
import com.cibo.evilplot.plot.{ContinuousBin, LegendContext, Plot, PlotContext}
import com.cibo.evilplot.plot.aesthetics.Theme


trait ContinuousBinRenderer extends PlotElementRenderer[ContinuousBin] {
  def render(plot: Plot, extent: Extent, bin: ContinuousBin): Drawable
  def legendContext: Option[LegendContext] = None
}


object ContinuousBinRenderer {

  def custom(
              renderFn: (PlotContext, ContinuousBin) => Drawable,
              legendCtx: Option[LegendContext] = None): ContinuousBinRenderer = new ContinuousBinRenderer {

    def render(plot: Plot, extent: Extent, bin: ContinuousBin): Drawable = {
      renderFn(PlotContext.from(plot, extent), bin)
    }

    override def legendContext: Option[LegendContext] = legendCtx
  }

  /** Default bar renderer. */
  def default(
               color: Option[Color] = None
             )(implicit theme: Theme): ContinuousBinRenderer = new ContinuousBinRenderer {
    def render(plot: Plot, extent: Extent, bin: ContinuousBin): Drawable = {
      Rect(extent.width, extent.height).filled(color.getOrElse(theme.colors.bar))
    }
  }
}
