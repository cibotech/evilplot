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
import com.cibo.evilplot.numeric.{Bounds, Point}
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.renderers.{BarRenderer, PlotRenderer}

object Histogram {

  val defaultBinCount: Int = 20

  // Create binCount bins from the given data and xbounds.
  private def createBins(values: Seq[Double], xbounds: Bounds, binCount: Int): Seq[Point] = {
    val binWidth = xbounds.range / binCount
    val grouped = values.groupBy { value => math.min(((value - xbounds.min) / binWidth).toInt, binCount - 1) }
    (0 until binCount).flatMap { i =>
      grouped.get(i).map { vs =>
        val y = vs.size
        val x = i * binWidth + xbounds.min
        Point(x, y)
      }
    }
  }

  case class HistogramRenderer(
    data: Seq[Double],
    barRenderer: BarRenderer,
    binCount: Int,
    spacing: Double,
    boundBuffer: Double
  ) extends PlotRenderer {
    def render(plot: Plot, plotExtent: Extent)(implicit theme: Theme): Drawable = {
      val xtransformer = plot.xtransform(plot, plotExtent)
      val ytransformer = plot.ytransform(plot, plotExtent)

      // The x bounds might have changed here, which could lead to a different binning of the data. If that
      // happens, it's possible for us to exceed our boundary. Thus we have two options:
      //  1. Clip at the boundary
      //  2. Scale all bars to have the correct relative heights.
      // Scaling the bars would show the correct histogram as long as no axis is displayed.  However, if
      // an axis is display, we would end up showing the wrong values. Thus, we clip if the y boundary is
      // fixed, otherwise we scale to make it look pretty.
      val points = createBins(data, plot.xbounds, binCount)
      val maxY = points.maxBy(_.y).y * (1.0 + boundBuffer)
      val yscale = if (plot.yfixed) 1.0 else math.min(1.0, plot.ybounds.max / maxY)

      val binWidth = plot.xbounds.range / binCount
      val yintercept = ytransformer(0)
      points.map { point =>
        val x = xtransformer(point.x) + spacing / 2.0
        val clippedY = math.min(point.y * yscale, plot.ybounds.max)
        val y = ytransformer(clippedY)
        val barWidth = math.max(xtransformer(point.x + binWidth) - x - spacing, 0)
        val bar = Bar(clippedY)
        val barHeight = yintercept - y
        barRenderer.render(plot, Extent(barWidth, barHeight), bar).translate(x = x, y = y)
      }.group
    }
  }

  /** Create a histogram.
    * @param values The data.
    * @param bins The number of bins to divide the data into.
    * @param barRenderer The renderer to render bars for each bin.
    * @param spacing The spacing between bars.
    * @param boundBuffer Extra padding to place at the top of the plot.
    * @return A histogram plot.
    */
  def apply(
    values: Seq[Double],
    bins: Int = defaultBinCount,
    barRenderer: Option[BarRenderer] = None,
    spacing: Option[Double] = None,
    boundBuffer: Option[Double] = None
  )(implicit theme: Theme): Plot = {
    require(bins > 0, "must have at least one bin")
    val xbounds = Bounds(values.min, values.max)
    val maxY = createBins(values, xbounds, bins).maxBy(_.y).y
    val binWidth = xbounds.range / bins
    Plot(
      xbounds = xbounds,
      ybounds = Bounds(0, maxY * (1.0 + boundBuffer.getOrElse(theme.elements.boundBuffer))),
      renderer = HistogramRenderer(
        values,
        barRenderer.getOrElse(BarRenderer.default()),
        bins,
        spacing.getOrElse(theme.elements.barSpacing),
        boundBuffer.getOrElse(theme.elements.boundBuffer)
      )
    )
  }
}
