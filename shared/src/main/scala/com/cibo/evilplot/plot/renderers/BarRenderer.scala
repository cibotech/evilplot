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
import com.cibo.evilplot.geometry._
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.{Bar, LegendContext, Plot, PlotContext}

trait BarRenderer extends PlotElementRenderer[Bar] {
  def render(plot: Plot, extent: Extent, category: Bar): Drawable
  def legendContext: Option[LegendContext] = None
}

object BarRenderer {

  def custom(
    renderFn: (PlotContext, Bar) => Drawable,
    legendCtx: Option[LegendContext] = None): BarRenderer = new BarRenderer {
    def render(plot: Plot, extent: Extent, category: Bar): Drawable = {
      renderFn(PlotContext.from(plot, extent), category)
    }

    override def legendContext: Option[LegendContext] = legendCtx
  }

  /** Default bar renderer. */
  def default(
    color: Option[Color] = None
  )(implicit theme: Theme): BarRenderer = new BarRenderer {
    def render(plot: Plot, extent: Extent, bar: Bar): Drawable = {
      Rect(extent.width, extent.height).filled(color.getOrElse(theme.colors.bar))
    }
  }

  /** A BarRenderer that assigns a single name to this bar. */
  def named(
    color: Option[Color] = None,
    name: Option[String] = None,
    legendElement: Option[Drawable] = None
  )(implicit theme: Theme): BarRenderer = new BarRenderer {
    def render(plot: Plot, extent: Extent, bar: Bar): Drawable = {
      Rect(extent.width, extent.height).filled(color.getOrElse(theme.colors.bar))
    }

    override def legendContext: Option[LegendContext] = name.map { n =>
      LegendContext.single(
        element = legendElement.getOrElse {
          val legSize = theme.fonts.legendLabelSize
          Rect(legSize, legSize).filled(color.getOrElse(theme.colors.bar))
        },
        label = n
      )
    }
  }

  /** Create a bar renderer to render a clustered bar chart. */
  def clustered(): BarRenderer = new BarRenderer {
    def render(plot: Plot, extent: Extent, bar: Bar): Drawable = {
      Rect(extent.width, extent.height).filled(bar.getColor(0))
    }
  }

  /** Create a bar renderer to render a stacked bar chart. */
  def stacked(): BarRenderer = new BarRenderer {
    def render(plot: Plot, extent: Extent, bar: Bar): Drawable = {
      val scale = if (bar.height == 0) 0.0 else extent.height / bar.height
      bar.values.zipWithIndex
        .map {
          case (value, stackIndex) =>
            val height = value * scale
            val width = extent.width
            Rect(width, height).filled(bar.getColor(stackIndex))
        }
        .reduce(_ below _)
    }
  }
}
