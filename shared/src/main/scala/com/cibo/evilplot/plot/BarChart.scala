package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent}
import com.cibo.evilplot.numeric.Bounds
import com.cibo.evilplot.plot.renderers.{BarRenderer, PlotRenderer}

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

  case class BarChartRenderer(
    barRenderer: BarRenderer,
    spacing: Double,
    groupSpacing: Double
  ) extends PlotRenderer[Seq[Bar]] {
    def render(plot: Plot[Seq[Bar]], plotExtent: Extent): Drawable = {
      val ytransformer = plot.ytransform(plot, plotExtent)


      val barCount = plot.data.size

      // Space used for bars. Space between each bar and half space at each left and right.
      val totalBarSpacing = barCount * spacing

      val numGroups = plot.data.map(_.group).distinct.size

      // Space used for groups. Same logic as for bars (except zero it out for 1 group).
      val groupPadding = if (numGroups == 1) 0 else numGroups * groupSpacing

      // The width of each bar.
      val barWidth = (plotExtent.width - groupPadding - totalBarSpacing) / barCount

      val sorted = plot.data.sortBy(_.group)
      val initial: (Double, Drawable) = (sorted.head.group, EmptyDrawable())
      sorted.zipWithIndex.foldLeft(initial) { case ((lastGroup, d), (bar, barIndex)) =>
        val y = ytransformer(math.abs(bar.height))
        val barHeight = ytransformer(0) - y
        val transY = if (bar.height < 0) y + barHeight else y
        val groupOffset =
          if (numGroups != 1 && bar.group != lastGroup) groupSpacing
          else 0
        val x =
          if (barIndex == 0) {
            (groupOffset + spacing) / 2
          } else {
            groupOffset + spacing
          }

        (bar.group, d beside barRenderer.render(Extent(barWidth, barHeight), plot.data, bar)
          .translate(y = transY, x = x))
      }._2
    }
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
      BarChartRenderer(barRenderer, spacing, groupSpacing)
    )
  }
}
