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

  def getColor(i: Int): Color = if (colors.lengthCompare(i) > 0) colors(i) else DefaultColors.barColor

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
      val xtransformer = plot.xtransform(plot, plotExtent)
      val ytransformer = plot.ytransform(plot, plotExtent)

      val numGroups = data.map(_.cluster).distinct.size
      val barsPerGroup = if (numGroups > 1) data.groupBy(_.cluster).map(_._2.size).max else 1

      val sorted = data.sortBy(_.cluster)
      val initial: (Double, Drawable) = (sorted.head.cluster, EmptyDrawable())
      sorted.zipWithIndex.foldLeft(initial) { case ((lastCluster, d), (bar, barIndex)) =>

        // X offset and bar width.
        val xscale = 1.0 / barsPerGroup
        val barx = barIndex * xscale
        val x = xtransformer(plot.xbounds.min + barx)
        val barWidth = xtransformer(plot.xbounds.min + barx + xscale) - x

        // Y offset and bar height.
        val y = ytransformer(math.min(math.abs(bar.height), plot.ybounds.max))
        val barHeight = ytransformer(math.max(0, plot.ybounds.min)) - y

        val transY = if (bar.height < 0) y + barHeight else y
        val clusterPadding = if (numGroups > 1 && bar.cluster != lastCluster) clusterSpacing else 0

        // Extra X offset to account for the cluster and spacing.
        val xPadding = if (barIndex == 0) (clusterPadding + spacing) / 2 else clusterPadding + spacing / 2

        val extent = Extent(barWidth - spacing - clusterPadding, barHeight)
        (bar.cluster, d behind barRenderer.render(plot, extent, bar).translate(y = transY, x = x + xPadding))
      }._2
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
    boundBuffer: Double = defaultBoundBuffer
  )(implicit theme: Theme): Plot = {
    val barRenderer = BarRenderer.default(color.getOrElse(theme.colors.bar))
    val bars = values.map(Bar(_))
    custom(bars, barRenderer, spacing, None, boundBuffer)
  }

  /** Create a bar chart where bars are divided into clusters. */
  def clustered(
    values: Seq[Seq[Double]],
    labels: Seq[String] = Seq.empty,
    colors: Seq[Color] = Color.stream,
    spacing: Option[Double] = None,
    clusterSpacing: Option[Double] = None,
    boundBuffer: Double = defaultBoundBuffer
  )(implicit theme: Theme): Plot = {
    val barRenderer = BarRenderer.clustered()
    val bars = values.zipWithIndex.flatMap { case (cluster, clusterIndex) =>
      cluster.zipWithIndex.map { case (value, index) =>
        val barLabel = if (clusterIndex == 0 && labels.lengthCompare(index) > 0) {
          Seq(Text(labels(index)))
        } else {
          Seq.empty[Drawable]
        }
        Bar(
          values = Seq(value),
          cluster = clusterIndex,
          colors = Seq(colors(index)),
          labels = barLabel
        )
      }
    }
    custom(bars, barRenderer, spacing, clusterSpacing, boundBuffer)
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
    boundBuffer: Double = defaultBoundBuffer
  )(implicit theme: Theme): Plot = {
    val barRenderer = BarRenderer.stacked()
    val barLabels = labels.map(l => Style(Text(l, theme.fonts.legendLabelSize), theme.colors.legendLabel))
    val colorStream = if (colors.nonEmpty) colors else theme.colors.stream
    val bars = values.map { stack =>
      Bar(stack, colors = colorStream, labels = barLabels, cluster = 0)
    }
    custom(bars, barRenderer, spacing, None, boundBuffer)
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
    colors: Seq[Color] = Color.stream,
    labels: Seq[String] = Seq.empty,
    spacing: Option[Double] = None,
    clusterSpacing: Option[Double] = None,
    boundBuffer: Double = defaultBoundBuffer
  )(implicit theme: Theme): Plot = {
    val barRenderer = BarRenderer.stacked()
    val barLabels = labels.map(Text(_))
    val bars = values.zipWithIndex.flatMap { case (cluster, clusterIndex) =>
      cluster.zipWithIndex.map { case (stack, barIndex) =>
        Bar(
          values = stack,
          cluster = clusterIndex,
          colors = colors,
          labels = barLabels
        )
      }
    }
    custom(bars, barRenderer, spacing, clusterSpacing, boundBuffer)
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
    barRenderer: BarRenderer = BarRenderer.default(),
    spacing: Option[Double] = None,
    clusterSpacing: Option[Double] = None,
    boundBuffer: Double = defaultBoundBuffer,
  )(implicit theme: Theme): Plot = {
    val xbounds = Bounds(0, bars.size)
    val ybounds = Plot.expandBounds(Bounds(bars.minBy(_.height).height, bars.maxBy(_.height).height), boundBuffer)
    Plot(
      xbounds,
      ybounds,
      BarChartRenderer(
        bars,
        barRenderer,
        spacing.getOrElse(theme.elements.barSpacing),
        clusterSpacing.getOrElse(theme.elements.clusterSpacing)
      )
    )
  }
}
