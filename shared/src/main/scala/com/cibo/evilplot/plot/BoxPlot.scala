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

import com.cibo.evilplot.geometry._
import com.cibo.evilplot.numeric.{Bounds, BoxPlotSummaryStatistics}
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.renderers.{BoxRenderer, PlotRenderer, PointRenderer}

private final case class BoxPlotRenderer(
  data: Seq[BoxPlotSummaryStatistics],
  boxRenderer: BoxRenderer,
  pointRenderer: PointRenderer,
  spacing: Double
) extends PlotRenderer {
  def render(plot: Plot, plotExtent: Extent)(implicit theme: Theme): Drawable = {
    val xtransformer = plot.xtransform(plot, plotExtent)
    val ytransformer = plot.ytransform(plot, plotExtent)

    data.zipWithIndex.foldLeft(EmptyDrawable(): Drawable) { case (d, (summary, index)) =>
      val x = xtransformer(plot.xbounds.min + index) + spacing / 2
      val y = ytransformer(summary.upperWhisker)

      val boxHeight = ytransformer(summary.lowerWhisker) - ytransformer(summary.upperWhisker)
      val boxWidth = xtransformer(plot.xbounds.min + index + 1) - x - spacing / 2

      val box = boxRenderer.render(plot, Extent(boxWidth, boxHeight), summary)

      val points = summary.outliers.map { pt =>
        pointRenderer.render(plot, plotExtent, index).translate(x = x + boxWidth / 2, y = ytransformer(pt))
      }
      d behind (box.translate(x = x, y = y) behind points.group)
    }
  }
}

object BoxPlot {
  /** Create a box plots for a sequence of distributions.
    * @param data the distributions to plot
    * @param quantiles quantiles to use for summary statistics.
    *                  defaults to 1st, 2nd, 3rd quartiles.
    * @param spacing spacing how much spacing to put between boxes
    * @param boundBuffer expand bounds by this factor
    * @param boxRenderer the `BoxRenderer` to use to display each distribution
    * @param pointRenderer the `PointRenderer` used to display outliers
    */
  def apply(
    data: Seq[Seq[Double]],
    quantiles: (Double, Double, Double) = (0.25, 0.50, 0.75),
    spacing: Option[Double] = None,
    boundBuffer: Option[Double] = None,
    boxRenderer: Option[BoxRenderer] = None,
    pointRenderer: Option[PointRenderer] = None
  )(implicit theme: Theme): Plot = {
    val summaries = data.map(dist => BoxPlotSummaryStatistics(dist, quantiles))
    val xbounds = Bounds(0, summaries.size - 1)
    val ybounds = Plot.expandBounds(
      Bounds(summaries.minBy(_.min).min, summaries.maxBy(_.max).max),
      boundBuffer.getOrElse(theme.elements.boundBuffer)
    )
    Plot(
      xbounds,
      ybounds,
      BoxPlotRenderer(
        summaries,
        boxRenderer.getOrElse(BoxRenderer.default()),
        pointRenderer.getOrElse(PointRenderer.default()),
        spacing.getOrElse(theme.elements.boxSpacing)
      )
    )
  }

  /** Create a box plots for a sequence of distributions.
    * @param data the distributions to plot
    * @param boxRenderer the `BoxRenderer` to use to display each distribution
    * @param pointRenderer the `PointRenderer` used to display outliers
    * @param quantiles quantiles to use for summary statistics.
    *                  defaults to 1st, 2nd, 3rd quartiles.
    * @param spacing spacing how much spacing to put between boxes
    * @param boundBuffer expand bounds by this factor
    */
  @deprecated("use apply", "2018-03-28")
  def custom(
    data: Seq[Seq[Double]],
    boxRenderer: BoxRenderer,
    pointRenderer: PointRenderer,
    quantiles: (Double, Double, Double) = (0.25, 0.50, 0.75),
    spacing: Option[Double] = None,
    boundBuffer: Option[Double] = None
  )(implicit theme: Theme): Plot = {
    val summaries = data.map(dist => BoxPlotSummaryStatistics(dist, quantiles))
    val xbounds = Bounds(0, summaries.size - 1)
    val ybounds = Plot.expandBounds(
      Bounds(summaries.minBy(_.min).min, summaries.maxBy(_.max).max),
      boundBuffer.getOrElse(theme.elements.boundBuffer)
    )
    Plot(
      xbounds,
      ybounds,
      BoxPlotRenderer(summaries, boxRenderer, pointRenderer, spacing.getOrElse(theme.elements.boxSpacing))
    )
  }
}
