package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent}
import com.cibo.evilplot.numeric.Bounds

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

    val initial: (Double, Drawable) = (0, EmptyDrawable())
    plot.data.sortBy(_.group).zipWithIndex.foldLeft(initial) { case ((lastGroup, d), (bar, barIndex)) =>
      val y = ytransformer(bar.height)
      val barHeight = plotExtent.height - y
      val x = if (bar.group == lastGroup) spacing else groupSpacing + spacing
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
    val xbounds = Bounds(0, bars.size)
    val ybounds = Bounds(
      bars.minBy(_.height).height * (1.0 - boundBuffer),
      bars.maxBy(_.height).height * (1.0 + boundBuffer)
    )
    Plot[Seq[Bar]](
      bars,
      xbounds,
      ybounds,
      renderBarChart(barRenderer, spacing, groupSpacing)
    )
  }
}
