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

package com.cibo.evilplot.plot.components

import com.cibo.evilplot.geometry.{Drawable, Extent}
import com.cibo.evilplot.plot.Plot
import com.cibo.evilplot.plot.Plot.Transformer
import com.cibo.evilplot.plot.aesthetics.Theme

case class BorderPlot(
  position: Position,
  borderSize: Double,
  border: Plot
) extends PlotComponent {
  case object InvertXTransformer extends Transformer {
    def apply(plot: Plot, plotExtent: Extent): Double => Double = {
      val scale = plotExtent.width / plot.xbounds.range
      (x: Double) =>
        plotExtent.width - (x - plot.xbounds.min) * scale
    }
  }

  override def size(plot: Plot): Extent = Extent(borderSize, borderSize)

  def render(plot: Plot, extent: Extent)(implicit theme: Theme): Drawable = {
    position match {
      case Position.Top =>
        border
          .xbounds(plot.xbounds)
          .copy(xtransform = plot.xtransform)
          .render(extent.copy(height = borderSize))
      case Position.Bottom =>
        val borderExent = extent.copy(height = borderSize)
        border
          .xbounds(plot.xbounds)
          .copy(xtransform = plot.xtransform)
          .render(borderExent)
          .rotated(180)
          .flipX
      case Position.Left =>
        val borderExtent = Extent(extent.height, borderSize)
        border
          .xbounds(plot.ybounds)
          .copy(xtransform = plot.xtransform)
          .render(borderExtent)
          .resize(borderExtent)
          .rotated(270)
      case Position.Right =>
        val borderExtent = Extent(extent.height, borderSize)
        border
          .xbounds(plot.ybounds)
          .setXTransform(InvertXTransformer, false)
          .render(borderExtent)
          .resize(borderExtent)
          .rotated(90)
      case _ =>
        border.render(extent)
    }
  }
}

trait BorderPlotImplicits {
  protected val plot: Plot

  val defaultSize: Double = 20

  /** Render a plot at the top border of the plot.
    * @param p The plot to render at top.
    */
  def topPlot(p: Plot, size: Double = defaultSize): Plot = plot :+ BorderPlot(Position.Top, size, p)

  /** Render a plot at the bottom border of the plot.
    * @param p The plot to render at bottom.
    */
  def bottomPlot(p: Plot, size: Double = defaultSize): Plot =
    plot :+ BorderPlot(Position.Bottom, size, p)

  /** Render a plot at the bottom border of the plot.
    * @param p The plot to render at bottom.
    */
  def leftPlot(p: Plot, size: Double = defaultSize): Plot =
    plot :+ BorderPlot(Position.Left, size, p)

  /** Render a plot at the right border of the plot.
    * @param p The plot to render at right.
    */
  def rightPlot(p: Plot, size: Double = defaultSize): Plot =
    plot :+ BorderPlot(Position.Right, size, p)
}
