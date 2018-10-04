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

import java.awt.geom.Point2D

import com.cibo.evilplot.geometry._
import com.cibo.evilplot.numeric.{Bounds, BoxPlotSummaryStatistics, Datum2d, Point2d}
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.renderers.BoxRenderer.BoxRendererContext
import com.cibo.evilplot.plot.renderers.{BoxRenderer, PlotRenderer, PointRenderer}

case class BoxPlotPoint(x: Double, y: Double, ctx: BoxRendererContext)
    extends Datum2d[BoxPlotPoint] {
  def withXY(x: Double, y: Double): BoxPlotPoint = this.copy(x = x, y = y)
}

final case class BoxPlotRenderer(
  data: Seq[Option[BoxRendererContext]],
  boxRenderer: BoxRenderer,
  pointRenderer: PointRenderer[BoxPlotPoint],
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

  def render(plot: Plot, plotExtent: Extent)(implicit theme: Theme): Drawable =
    render(PlotContext.from(plot, plotExtent))

  def render(pCtx: PlotContext)(implicit theme: Theme): Drawable = {
    val xtransformer = pCtx.xCartesianTransform
    val ytransformer = pCtx.yCartesianTransform

    val fullPlotWidth = xtransformer(pCtx.xBounds.min + numGroups)
    val clusterWidth = fullPlotWidth / numGroups - clusterPadding
    val boxWidth = (clusterWidth - (boxesPerGroup - 1) * spacing) / boxesPerGroup

    data.foldLeft(EmptyDrawable(): Drawable) {
      case (d, boxContextOpt) =>
        boxContextOpt match {
          case Some(boxContext) =>
            import boxContext.{index, cluster, summaryStatistics}

            val x = getBoxX(index, cluster, boxWidth, clusterWidth)
            val y = ytransformer(summaryStatistics.upperWhisker)

            val boxHeight = ytransformer(summaryStatistics.lowerWhisker) - ytransformer(
              summaryStatistics.upperWhisker)

            val box = {
              if (boxHeight != 0)
                boxRenderer.render(
                  pCtx.plot,
                  Extent(boxWidth, boxHeight),
                  BoxRendererContext(summaryStatistics, index))
              else {
                StrokeStyle(Line(boxWidth, theme.elements.strokeWidth), theme.colors.path)
              }
            }

            val points = summaryStatistics.outliers.map { pt =>
              pointRenderer
                .render(pCtx.extent, BoxPlotPoint(x, y, boxContext))
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
    pointRenderer: Option[PointRenderer[BoxPlotPoint]] = None
  )(implicit theme: Theme): Plot = {
    val boxContexts = data.zipWithIndex.map {
      case (dist, index) =>
        if (dist.nonEmpty) {
          val summary = BoxPlotSummaryStatistics(dist, quantiles)
          Some(BoxRendererContext(summary, index))
        } else None
    }
    makePlot(
      data,
      boxContexts,
      spacing,
      None,
      boundBuffer,
      boxRenderer,
      pointRenderer
    )
  }

  /** Create clustered box plots for a sequence of distributions.
    *
    * @param data the clusters of distributions to plot
    * @param boxRenderer the `BoxRenderer` to use to display each distribution
    * @param pointRenderer the `PointRenderer` used to display outliers
    * @param quantiles quantiles to use for summary statistics.
    *                  defaults to 1st, 2nd, 3rd quartiles.
    * @param spacing spacing how much spacing to put between boxes
    * @param clusterSpacing how much spacing to put between clusters of boxes
    * @param boundBuffer expand bounds by this factor
    */
  def clustered(
    data: Seq[Seq[Seq[Double]]],
    quantiles: (Double, Double, Double) = (0.25, 0.50, 0.75),
    spacing: Option[Double] = None,
    clusterSpacing: Option[Double] = None,
    boundBuffer: Option[Double] = None,
    boxRenderer: Option[BoxRenderer] = None,
    pointRenderer: Option[PointRenderer[BoxPlotPoint]] = None
  )(implicit theme: Theme): Plot = {
    val boxesPerGroup = data.map(_.length).reduce(math.max)
    val boxContexts = data.zipWithIndex.flatMap {
      case (cluster, clusterIndex) =>
        cluster.zipWithIndex.map {
          case (dist, index) =>
            if (dist.nonEmpty) {
              val barIndex = index + clusterIndex * boxesPerGroup
              val summary = BoxPlotSummaryStatistics(dist, quantiles)
              Some(BoxRendererContext(summary, barIndex, clusterIndex))
            } else None
        }
    }
    makePlot(
      data.flatten,
      boxContexts,
      spacing,
      Some(clusterSpacing.getOrElse(theme.elements.clusterSpacing)),
      boundBuffer,
      boxRenderer,
      pointRenderer
    )
  }

  private def makePlot(
    data: Seq[Seq[Double]],
    boxContexts: Seq[Option[BoxRendererContext]],
    spacing: Option[Double] = None,
    clusterSpacing: Option[Double] = None,
    boundBuffer: Option[Double] = None,
    boxRenderer: Option[BoxRenderer] = None,
    pointRenderer: Option[PointRenderer[BoxPlotPoint]] = None
  )(implicit theme: Theme): Plot = {
    val xbounds = Bounds(0, boxContexts.size)
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
        boxContexts,
        boxRenderer.getOrElse(BoxRenderer.default()),
        pointRenderer.getOrElse(PointRenderer.default[BoxPlotPoint]()),
        spacing.getOrElse(theme.elements.boxSpacing),
        clusterSpacing
      )
    )
  }

}
