package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent}
import com.cibo.evilplot.numeric.Bounds
import com.cibo.evilplot.plot.renderers.BarRenderer

final case class Bar(values: Seq[Double], group: Int = 0) {
  lazy val height: Double = values.sum
}

object Bar {
  def apply(value: Double): Bar = Bar(Seq(value))
  def apply(value: Double, group: Int): Bar = Bar(Seq(value), group)
}

object BarChart {

  val defaultBoundBuffer: Double = 0.1
  val defaultSpacing: Double = 1.0
  val defaultGroupSpacing: Double = 4.0

  private def renderBarChart(
    barRenderer: BarRenderer,
    spacing: Double,
    groupSpacing: Double
  )(plot: Plot[Seq[Bar]], plotExtent: Extent): Drawable = {
    val ytransformer = plot.ytransform(plot, plotExtent)

    // Space used for groups.
    val groupPadding = (plot.data.map(_.group).distinct.size - 1) * groupSpacing

    // Total bar spacing used.
    val barCount = plot.data.size
    val totalBarSpacing = (barCount - 1) * spacing

    // The width of each bar.
    val barWidth = (plotExtent.width - groupPadding - totalBarSpacing) / barCount

    val sorted = plot.data.sortBy(_.group)
    val initial: (Double, Drawable) = (sorted.head.group, EmptyDrawable())
    sorted.zipWithIndex.foldLeft(initial) { case ((lastGroup, d), (bar, barIndex)) =>
      val y = ytransformer(bar.height)
      val barHeight = plotExtent.height - y
      val x =
        if (barIndex == 0) {
          0
        } else if (bar.group == lastGroup) {
          spacing
        } else {
          groupSpacing + spacing
        }
      (bar.group, d beside barRenderer.render(bar, Extent(barWidth, barHeight), barIndex).translate(y = y, x = x))
    }._2
  }

  def apply(
    bars: Seq[Bar],
    barRenderer: BarRenderer = BarRenderer.default(),
    spacing: Double = defaultSpacing,
    groupSpacing: Double = defaultGroupSpacing,
    boundBuffer: Double = defaultBoundBuffer
  ): Plot[Seq[Bar]] = {
    val xbounds = Bounds(0, bars.size - 1)
    val ybounds = Plot.expandBounds(Bounds(bars.minBy(_.height).height, bars.maxBy(_.height).height), boundBuffer)
    Plot[Seq[Bar]](
      bars,
      xbounds,
      ybounds,
      renderBarChart(barRenderer, spacing, groupSpacing)
    )
  }
}
