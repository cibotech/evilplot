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
import com.cibo.evilplot.plot.renderers.BoxRenderer.BoxRendererContext
import com.cibo.evilplot.plot.renderers.{BoxRenderer, PlotRenderer, PointRenderer}

final case class BoxPlotRenderer(
  data: Seq[Option[BoxPlotSummaryStatistics]],
  boxRenderer: BoxRenderer,
  pointRenderer: PointRenderer,
  spacing: Double,
  clusterSpacing: Option[Double]
) extends PlotRenderer {

  private val isClustered = clusterSpacing.isDefined
  private val clusterPadding = clusterSpacing.getOrElse(spacing)
  private val numGroups = data.flatten.map(_.cluster).distinct.size
  private val boxesPerGroup =
    if (isClustered) data.flatten.groupBy(_.cluster).map(_._2.size).max else 1

  private def getBoxX(
    boxIndex: Int,
    cluster: Int,
    boxWidth: Double,
    clusterWidth: Double): Double = {
    val clusterIndex = if (isClustered) cluster else boxIndex
    val clusterStartX = clusterPadding / 2 + (clusterWidth + clusterPadding) * clusterIndex
    val boxXInCluster = (boxWidth + spacing) * (boxIndex % boxesPerGroup)
    clusterStartX + boxXInCluster
  }

  def render(plot: Plot, plotExtent: Extent)(implicit theme: Theme): Drawable = {
    val xtransformer = plot.xtransform(plot, plotExtent)
    val ytransformer = plot.ytransform(plot, plotExtent)

    val fullPlotWidth = xtransformer(plot.xbounds.min + numGroups)
    val clusterWidth = fullPlotWidth / numGroups - clusterPadding
    val boxWidth = (clusterWidth - (boxesPerGroup - 1) * spacing) / boxesPerGroup

    data.zipWithIndex.foldLeft(EmptyDrawable(): Drawable) {
      case (d, (summaryOpt, index)) =>
        summaryOpt match {
          case Some(summary) =>
            val x = getBoxX(index, summary.cluster, boxWidth, clusterWidth)
            val y = ytransformer(summary.upperWhisker)

            val boxHeight = ytransformer(summary.lowerWhisker) - ytransformer(summary.upperWhisker)

            val box = {
              if (boxHeight != 0)
                boxRenderer.render(
                  plot,
                  Extent(boxWidth, boxHeight),
                  BoxRendererContext(summary, index))
              else {
                StrokeStyle(Line(boxWidth, theme.elements.strokeWidth), theme.colors.path)
              }
            }

            val points = summary.outliers.map { pt =>
              pointRenderer
                .render(plot, plotExtent, index)
                .translate(x = x + boxWidth / 2, y = ytransformer(pt))
            }
            d behind (box.translate(x = x, y = y) behind points.group)
          case None => d
        }
    }
  }

  override def legendContext: LegendContext = boxRenderer.legendContext
}

object BoxPlot {

  /** Create box plots for a sequence of distributions.
    *
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
    val summaries =
      data.map(dist => if (dist.nonEmpty) Some(BoxPlotSummaryStatistics(dist, quantiles)) else None)
    custom(summaries, boxRenderer, pointRenderer, quantiles, spacing, None, boundBuffer)
  }

  /** Create clustered box plots for a sequence of distributions.
    *
    * @param data the clusters of distributions to plot
    * @param boxRenderer the `BoxRenderer` to use to display each distribution
    * @param pointRenderer the `PointRenderer` used to display outliers
    * @param quantiles quantiles to use for summary statistics.
    *                  defaults to 1st, 2nd, 3rd quartiles.
    * @param spacing spacing how much spacing to put between boxes
    * @param clusterSpacing ho much spacing to put between clusters of boxes
    * @param boundBuffer expand bounds by this factor
    */
  def clustered(
    data: Seq[Seq[Seq[Double]]],
    quantiles: (Double, Double, Double) = (0.25, 0.50, 0.75),
    spacing: Option[Double] = None,
    clusterSpacing: Option[Double] = None,
    boundBuffer: Option[Double] = None,
    boxRenderer: Option[BoxRenderer] = None,
    pointRenderer: Option[PointRenderer] = None
  )(implicit theme: Theme): Plot = {
    println(data.length)
    val summaries = data.zipWithIndex.flatMap {
      case (cluster, clusterIndex) =>
        cluster.map(
          dist =>
            if (dist.nonEmpty) Some(BoxPlotSummaryStatistics(dist, quantiles, cluster = clusterIndex))
            else None)
    }
    custom(
      summaries,
      boxRenderer,
      pointRenderer,
      quantiles,
      spacing,
      Some(clusterSpacing.getOrElse(theme.elements.clusterSpacing)),
      boundBuffer)
  }

  /** Create custom box plots for a sequence of distributions.
    *
    * @param data the distributions to plot
    * @param boxRenderer the `BoxRenderer` to use to display each distribution
    * @param pointRenderer the `PointRenderer` used to display outliers
    * @param quantiles quantiles to use for summary statistics.
    *                  defaults to 1st, 2nd, 3rd quartiles.
    * @param spacing spacing how much spacing to put between boxes
    * @param boundBuffer expand bounds by this factor
    */
  def custom(
    summaries: Seq[Option[BoxPlotSummaryStatistics]],
    boxRenderer: Option[BoxRenderer],
    pointRenderer: Option[PointRenderer],
    quantiles: (Double, Double, Double) = (0.25, 0.50, 0.75),
    spacing: Option[Double] = None,
    clusterSpacing: Option[Double] = None,
    boundBuffer: Option[Double] = None
  )(implicit theme: Theme): Plot = {
    val data = summaries.flatten.map(_.allPoints)
    val xbounds = Bounds(0, summaries.size)
    val ybounds = Plot.expandBounds(
      Bounds(
        data.flatten.reduceOption[Double](math.min).getOrElse(0),
        data.flatten.reduceOption[Double](math.max).getOrElse(0)
      ),
      boundBuffer.getOrElse(theme.elements.boundBuffer)
    )
    Plot(
      xbounds,
      ybounds,
      BoxPlotRenderer(
        summaries,
        boxRenderer.getOrElse(BoxRenderer.default()),
        pointRenderer.getOrElse(PointRenderer.default()),
        spacing.getOrElse(theme.elements.boxSpacing),
        clusterSpacing
      )
    )
  }
}
