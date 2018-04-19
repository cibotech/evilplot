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

import com.cibo.evilplot.colors.{Color, ScaledColorBar}
import com.cibo.evilplot.geometry.{Drawable, Extent, Rect}
import com.cibo.evilplot.numeric.Bounds
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.renderers.PlotRenderer

object Heatmap {

  val defaultColorCount: Int = 10

  private case class HeatmapRenderer(
    data: Seq[Seq[Double]],
    colorBar: ScaledColorBar
  )(implicit theme: Theme) extends PlotRenderer {
    override def legendContext: LegendContext = LegendContext.fromColorBar(colorBar)
    def render(plot: Plot, plotExtent: Extent)(implicit theme: Theme): Drawable = {
      val xtransformer = plot.xtransform(plot, plotExtent)
      val ytransformer = plot.ytransform(plot, plotExtent)
      val rowCount = data.size

      data.zipWithIndex.map { case (row, yIndex) =>
        row.zipWithIndex.map { case (value, xIndex) =>
          val y = ytransformer(yIndex)
          val x = xtransformer(xIndex)
          val width = xtransformer(xIndex + 1) - x
          val height = -(ytransformer(yIndex + 1) - y)
          Rect(width, height).filled(colorBar.getColor(value)).translate(x, y - height)
        }.group
      }.group
    }
  }

  /** Create a heatmap using a ScaledColorBar.
    * @param data The heatmap data.
    * @param colorBar The color bar to use.
    */
  def apply(
    data: Seq[Seq[Double]],
    colorBar: ScaledColorBar
  )(implicit theme: Theme): Plot = {
    val xbounds = Bounds(0, data.foldLeft(0)((a, s) => math.max(a, s.size)))
    val ybounds = Bounds(0, data.size)
    Plot(
      xbounds = xbounds,
      ybounds = ybounds,
      xfixed = true,
      yfixed = true,
      renderer = HeatmapRenderer(data, colorBar)
    )
  }

  /** Create a heatmap using a color sequence.
    * @param data The heatmap data.
    * @param colorCount The number of colors to use from the sequence.
    * @param colors The color sequence.
    */
  def apply(
    data: Seq[Seq[Double]],
    colorCount: Int = defaultColorCount,
    colors: Seq[Color] = Seq.empty
  )(implicit theme: Theme): Plot = {
    val colorStream = if (colors.nonEmpty) colors else theme.colors.stream
    val flattenedData = data.flatten
    val minValue = flattenedData.reduceOption[Double](math.min).getOrElse(0.0)
    val maxValue = flattenedData.reduceOption[Double](math.max).getOrElse(0.0)
    val colorBar = ScaledColorBar(colorStream.take(colorCount), minValue, maxValue)
    apply(data, colorBar)
  }
}
