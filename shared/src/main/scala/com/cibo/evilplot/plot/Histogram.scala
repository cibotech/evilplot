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

import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent}
import com.cibo.evilplot.numeric.{Bounds, Point}
import com.cibo.evilplot.plot.Histogram.ContinuousBinPlotRenderer
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.renderers.{BarRenderer, PlotRenderer}

object Histogram {

  val defaultBinCount: Int = 20

  /** Create binCount bins from the given data and xbounds.
    * @param values the raw data
    * @param xbounds the bounds over which to bin
    * @param binCount the number of bins to create
    * @return a sequence of points, where the x coordinates represent the left
    *         edge of the bins and the y coordinates represent their heights
    */
  def createBins(values: Seq[Double], xbounds: Bounds, binCount: Int): Seq[Point] =
    createBins(values, xbounds, binCount, normalize = false, cumulative = false)

  /** Create binCount bins from the given data and xbounds, normalizing the heights
    * such that their sum is 1 */
  def normalize(values: Seq[Double], xbounds: Bounds, binCount: Int): Seq[Point] =
    createBins(values, xbounds, binCount, normalize = true, cumulative = false)

  /** Create binCount bins from the given data and xbounds, cumulatively
    * such that each bin includes the data in all previous bins */
  def cumulative(values: Seq[Double], xbounds: Bounds, binCount: Int): Seq[Point] =
    createBins(values, xbounds, binCount, normalize = false, cumulative = true)

  /** Create binCount bins from the given data and xbounds, computing the bin
    * heights such that they represent the average probability density over each
    * bin interval */
  def density(values: Seq[Double], xbounds: Bounds, binCount: Int): Seq[Point] = {
    val binWidth = xbounds.range / binCount
    createBins(values, xbounds, binCount, normalize = true, cumulative = false)
      .map { case Point(x, y) => Point(x, y / binWidth) }
  }

  /** Create binCount bins from the given data and xbounds, cumulatively
    * such that each bin includes the data in all previous bins, and normalized
    * so that bins approximate a CDF */
  def cumulativeDensity(values: Seq[Double], xbounds: Bounds, binCount: Int): Seq[Point] =
    createBins(values, xbounds, binCount, normalize = true, cumulative = true)

  // Create binCount bins from the given data and xbounds.
  private def createBins(
    values: Seq[Double],
    xbounds: Bounds,
    binCount: Int,
    normalize: Boolean,
    cumulative: Boolean): Seq[Point] = {
    val binWidth = xbounds.range / binCount

    val grouped = values.groupBy { value =>
      math.min(((value - xbounds.min) / binWidth).toInt, binCount - 1)
    }
    val pts = (0 until binCount).map { i =>
      val x = i * binWidth + xbounds.min
      grouped.get(i) match {
        case Some(vs) =>
          val y = if (normalize) vs.size.toDouble / values.size else vs.size
          Point(x, y)
        case _ => Point(x, 0)
      }
    }
    if (cumulative) {
      pts.scanLeft(Point(0, 0)) { case (Point(_, t), Point(x, y)) => Point(x, y + t) }.drop(1)
    } else {
      pts
    }
  }

  case class HistogramRenderer(
    data: Seq[Double],
    barRenderer: BarRenderer,
    binCount: Int,
    spacing: Double,
    boundBuffer: Double,
    binningFunction: (Seq[Double], Bounds, Int) => Seq[Point])
      extends PlotRenderer {
    def render(plot: Plot, plotExtent: Extent)(implicit theme: Theme): Drawable = {
      if (data.nonEmpty) {

        val xtransformer = plot.xtransform(plot, plotExtent)
        val ytransformer = plot.ytransform(plot, plotExtent)

        // The x bounds might have changed here, which could lead to a different binning of the data. If that
        // happens, it's possible for us to exceed our boundary. Thus we have two options:
        //  1. Clip at the boundary
        //  2. Scale all bars to have the correct relative heights.
        // Scaling the bars would show the correct histogram as long as no axis is displayed.  However, if
        // an axis is display, we would end up showing the wrong values. Thus, we clip if the y boundary is
        // fixed, otherwise we scale to make it look pretty.
        val points = binningFunction(data, plot.xbounds, binCount)
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
      } else {
        EmptyDrawable()
      }
    }

    override val legendContext: LegendContext =
      barRenderer.legendContext.getOrElse(LegendContext.empty)

  }

  case class ContinuousBinPlotRenderer(
    bins: Seq[ContinuousBin],
    binRenderer: ContinuousBinRenderer,
    spacing: Double,
    boundBuffer: Double)
      extends PlotRenderer {

    def render(plot: Plot, plotExtent: Extent)(implicit theme: Theme): Drawable = {
      if (bins.nonEmpty) {

        val xtransformer = plot.xtransform(plot, plotExtent)
        val ytransformer = plot.ytransform(plot, plotExtent)

        val maxY = bins.maxBy(_.y).y * (1.0 + boundBuffer)
        val yscale = if (plot.yfixed) 1.0 else math.min(1.0, plot.ybounds.max / maxY)

        val yintercept = ytransformer(0)

        bins.map { bin =>
          val x = xtransformer(bin.x.min) + spacing / 2.0
          val clippedY = math.min(bin.y * yscale, plot.ybounds.max)
          val y = ytransformer(clippedY)
          val barWidth = math.max(xtransformer(bin.x.range + plot.xbounds.min) - spacing, 0)

          val barHeight = yintercept - y
          binRenderer.render(plot, Extent(barWidth, barHeight), bin).translate(x = x, y = y)
        }.group
      } else {
        EmptyDrawable()
      }
    }

    override val legendContext: LegendContext =
      binRenderer.legendContext.getOrElse(LegendContext.empty)

  }

  /** Create a histogram.
    * @param values The data.
    * @param bins The number of bins to divide the data into.
    * @param barRenderer The renderer to render bars for each bin.
    * @param spacing The spacing between bars.
    * @param boundBuffer Extra padding to place at the top of the plot.
    * @param binningFunction A function taking the raw data, the x bounds, and a bin count
    *                        that returns a sequence of points with x points representing left
    *                        bin boundaries and y points representing bin heights
    * @return A histogram plot.
    */
  def apply(
    values: Seq[Double],
    bins: Int = defaultBinCount,
    barRenderer: Option[BarRenderer] = None,
    spacing: Option[Double] = None,
    boundBuffer: Option[Double] = None,
    binningFunction: (Seq[Double], Bounds, Int) => Seq[Point] = createBins)(
    implicit theme: Theme): Plot = {
    require(bins > 0, "must have at least one bin")
    val xbounds = Bounds(
      values.reduceOption[Double](math.min).getOrElse(0.0),
      values.reduceOption[Double](math.max).getOrElse(0.0)
    )
    val maxY =
      binningFunction(values, xbounds, bins).map(_.y).reduceOption[Double](math.max).getOrElse(0.0)

    Plot(
      xbounds = xbounds,
      ybounds = Bounds(0, maxY * (1.0 + boundBuffer.getOrElse(theme.elements.boundBuffer))),
      renderer = HistogramRenderer(
        values,
        barRenderer.getOrElse(BarRenderer.default()),
        bins,
        spacing.getOrElse(theme.elements.barSpacing),
        boundBuffer.getOrElse(theme.elements.boundBuffer),
        binningFunction
      )
    )
  }

  def fromBins(
    bins: Seq[ContinuousBin],
    binRenderer: Option[ContinuousBinRenderer] = None,
    spacing: Option[Double] = None,
    boundBuffer: Option[Double] = None)(
    implicit theme: Theme): Plot = {
    require(bins.nonEmpty, "must have at least one bin")
    val xbounds = Bounds.union(bins.map(_.x))
    val maxY = bins.map(_.y).max

    Plot(
      xbounds = xbounds,
      ybounds = Bounds(0, maxY * (1.0 + boundBuffer.getOrElse(theme.elements.boundBuffer))),
      renderer = ContinuousBinPlotRenderer(
        bins,
        binRenderer.getOrElse(ContinuousBinRenderer.default()),
        spacing.getOrElse(theme.elements.barSpacing),
        boundBuffer.getOrElse(theme.elements.boundBuffer)
      )
    )
  }
}
