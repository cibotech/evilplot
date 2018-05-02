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

import com.cibo.evilplot.colors.{Color, DefaultColors}
import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent, Rect, Style, Text}
import com.cibo.evilplot.numeric.Bounds
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.renderers.{BarRenderer, PlotRenderer}

/** Data for a bar in a bar chart.
  * @param values The values (one per stack, starting at the bottom).
  * @param colors Colors for each bar segment (optional).
  * @param labels Labels for each bar segment (optional, for legends).
  * @param cluster The cluster to which this bar belongs.
  */
final case class Bar(
  values: Seq[Double],
  cluster: Int,
  colors: Seq[Color],
  labels: Seq[Drawable] = Seq.empty
) {
  lazy val height: Double = values.sum

  def getColor(i: Int): Color =
    if (colors.lengthCompare(i) > 0) colors(i) else DefaultColors.barColor

  def legendContext: LegendContext = LegendContext(
    elements = labels.zip(colors).map(lc => Rect(Text.defaultSize, Text.defaultSize).filled(lc._2)),
    labels = labels.zip(colors).map(lc => lc._1),
    defaultStyle = LegendStyle.Categorical
  )
}

object Bar {
  def apply(value: Double)(implicit theme: Theme): Bar = Bar(Seq(value), 0, theme.colors.stream)
  def apply(value: Double, cluster: Int)(implicit theme: Theme): Bar =
    Bar(Seq(value), cluster = cluster, theme.colors.stream)
}

object BarChart {

  val defaultBoundBuffer: Double = 0.1

  case class BarChartRenderer(
    data: Seq[Bar],
    barRenderer: BarRenderer,
    spacing: Double,
    clusterSpacing: Double
  ) extends PlotRenderer {

    override def legendContext: LegendContext = LegendContext.combine(data.map(_.legendContext))

    def render(plot: Plot, plotExtent: Extent)(implicit theme: Theme): Drawable = {
      if (data.isEmpty) {
        EmptyDrawable()
      } else {
        val xtransformer = plot.xtransform(plot, plotExtent)
        val ytransformer = plot.ytransform(plot, plotExtent)

        val numGroups = data.map(_.cluster).distinct.size
        val barsPerGroup = if (numGroups > 1) data.groupBy(_.cluster).map(_._2.size).max else 1

        val sorted = data.sortBy(_.cluster)
        val initial: (Double, Drawable) = (sorted.head.cluster, EmptyDrawable())
        sorted.zipWithIndex
          .foldLeft(initial) {
            case ((lastCluster, d), (bar, barIndex)) =>
              // X offset and bar width.
              val xscale = 1.0 / barsPerGroup
              val barx = barIndex * xscale
              val x = xtransformer(plot.xbounds.min + barx)
              val barWidth = xtransformer(plot.xbounds.min + barx + xscale) - x

              // Y bar translation and bar height.
              val (transY, barHeight) =
                if (plot.ybounds.isInBounds(0)) {
                  val y = ytransformer(math.abs(bar.height))
                  val height = ytransformer(math.max(0, plot.ybounds.min)) - y
                  (if (bar.height < 0) y + height else y, height)
                } else {
                  if (plot.ybounds.min > 0) {
                    val y = math.abs(ytransformer(bar.height) - ytransformer(plot.ybounds.min))
                    (plotExtent.height - y, y)
                  } else {
                    val y = math.abs(ytransformer(plot.ybounds.max) - ytransformer(bar.height))
                    (0d, y)
                  }
                }

              val clusterPadding =
                if (numGroups > 1 && bar.cluster != lastCluster) clusterSpacing else 0

              // Extra X offset to account for the cluster and spacing.
              val xPadding =
                if (barIndex == 0) (clusterPadding + spacing) / 2 else clusterPadding + spacing / 2

              val extent = Extent(barWidth - spacing - clusterPadding, barHeight)
              (
                bar.cluster,
                d behind barRenderer
                  .render(plot, extent, bar)
                  .translate(y = transY, x = x + xPadding))
          }
          ._2
      }
    }
  }

  /** Create a bar chart where the bars differ only by height
    * @param values The height values for the bars.
    * @param color Bar color.
    * @param spacing Bar spacing.
    * @param boundBuffer Extra padding above the plot area.
    */
  def apply(
    values: Seq[Double],
    color: Option[Color] = None,
    spacing: Option[Double] = None,
    boundBuffer: Option[Double] = None
  )(implicit theme: Theme): Plot = {
    val barRenderer = BarRenderer.default(color)
    val bars = values.map(Bar(_))
    custom(bars, Some(barRenderer), spacing, None, boundBuffer)
  }

  /** Create a bar chart where bars are divided into clusters. */
  def clustered(
    values: Seq[Seq[Double]],
    labels: Seq[String] = Seq.empty,
    colors: Seq[Color] = Seq.empty,
    spacing: Option[Double] = None,
    clusterSpacing: Option[Double] = None,
    boundBuffer: Option[Double] = None
  )(implicit theme: Theme): Plot = {
    val barRenderer = BarRenderer.clustered()
    val colorStream = if (colors.nonEmpty) colors else theme.colors.stream
    val bars = values.zipWithIndex.flatMap {
      case (cluster, clusterIndex) =>
        cluster.zipWithIndex.map {
          case (value, index) =>
            val barLabel = if (clusterIndex == 0 && labels.lengthCompare(index) > 0) {
              Seq(
                Style(
                  Text(
                    labels(index),
                    size = theme.fonts.legendLabelSize,
                    fontFace = theme.fonts.fontFace),
                  theme.colors.legendLabel))
            } else {
              Seq.empty[Drawable]
            }
            Bar(
              values = Seq(value),
              cluster = clusterIndex,
              colors = Seq(colorStream(index)),
              labels = barLabel
            )
        }
    }
    custom(bars, Some(barRenderer), spacing, clusterSpacing, boundBuffer)
  }

  /** Create a stacked bar chart.
    * @param values A sequence of bars where each bar is a sequence of values.
    * @param colors The color to use for each slice.
    * @param labels Labels for each slice.
    * @param spacing The bar spacing.
    * @param boundBuffer Extra padding at the top of the plot.
    */
  def stacked(
    values: Seq[Seq[Double]],
    colors: Seq[Color] = Seq.empty,
    labels: Seq[String] = Seq.empty,
    spacing: Option[Double] = None,
    boundBuffer: Option[Double] = None
  )(implicit theme: Theme): Plot = {
    val barRenderer = BarRenderer.stacked()
    val barLabels = labels.map(l =>
      Style(Text(l, theme.fonts.legendLabelSize, theme.fonts.fontFace), theme.colors.legendLabel))
    val colorStream = if (colors.nonEmpty) colors else theme.colors.stream
    val bars = values.map { stack =>
      Bar(stack, colors = colorStream, labels = barLabels, cluster = 0)
    }
    custom(bars, Some(barRenderer), spacing, None, boundBuffer)
  }

  /** Create a clustered bar chart of stacked bars.
    * @param values A sequence of clusters of bars where each bar is a sequence of values.
    * @param colors The color to use for each slice of a stacked bar.
    * @param labels Labels for each color in the stacked bar.
    * @param spacing Spacing between bars.
    * @param clusterSpacing Spacing between clusters.
    * @param boundBuffer Padding above the bar chart.
    */
  def clusteredStacked(
    values: Seq[Seq[Seq[Double]]],
    colors: Seq[Color] = Seq.empty,
    labels: Seq[String] = Seq.empty,
    spacing: Option[Double] = None,
    clusterSpacing: Option[Double] = None,
    boundBuffer: Option[Double] = None
  )(implicit theme: Theme): Plot = {
    val barRenderer = BarRenderer.stacked()
    val barLabels = labels.map(l =>
      Style(Text(l, theme.fonts.legendLabelSize, theme.fonts.fontFace), theme.colors.legendLabel))
    val colorStream = if (colors.nonEmpty) colors else theme.colors.stream
    val bars = values.zipWithIndex.flatMap {
      case (cluster, clusterIndex) =>
        cluster.zipWithIndex.map {
          case (stack, barIndex) =>
            Bar(
              values = stack,
              cluster = clusterIndex,
              colors = colorStream,
              labels = barLabels
            )
        }
    }
    custom(bars, Some(barRenderer), spacing, clusterSpacing, boundBuffer)
  }

  /** Create a custom bar chart.
    * @param bars The bars to render.
    * @param barRenderer The renderer to use.
    * @param spacing Spacing between bars within a cluster.
    * @param clusterSpacing Spacing between clusters.
    * @param boundBuffer Extra padding above the plot area.
    */
  def custom(
    bars: Seq[Bar],
    barRenderer: Option[BarRenderer] = None,
    spacing: Option[Double] = None,
    clusterSpacing: Option[Double] = None,
    boundBuffer: Option[Double] = None
  )(implicit theme: Theme): Plot = {
    val xbounds = Bounds(0, bars.size)
    val heights = bars.map(_.height)
    val ybounds = Plot.expandBounds(
      Bounds(
        heights.reduceOption[Double](math.min).getOrElse(0),
        heights.reduceOption[Double](math.max).getOrElse(0)),
      boundBuffer.getOrElse(theme.elements.boundBuffer)
    )
    Plot(
      xbounds,
      ybounds,
      BarChartRenderer(
        bars,
        barRenderer.getOrElse(BarRenderer.default()),
        spacing.getOrElse(theme.elements.barSpacing),
        clusterSpacing.getOrElse(theme.elements.clusterSpacing)
      )
    )
  }
}
