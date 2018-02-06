package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.{Drawable, Extent, Translate}
import com.cibo.evilplot.numeric.Bounds

object BarChart {

  val defaultBoundBuffer: Double = 0.1

  private def renderBarChart(barRenderer: BarRenderer)(plot: Plot[Seq[Bar]], plotExtent: Extent): Drawable = {
    val xtransformer = plot.xtransform(plot, plotExtent)
    val ytransformer = plot.ytransform(plot, plotExtent)
    plot.data.zipWithIndex.map { case (bar, barIndex) =>
      val x = xtransformer(barIndex)
      val y = ytransformer(bar.height)
      val barWidth = xtransformer(barIndex + 1) - x
      val barHeight = plotExtent.height - y
      Translate(
        barRenderer.render(bar, Extent(barWidth, barHeight), barIndex),
        x = x,
        y = y
      )
    }.group
  }

  def apply(
    bars: Seq[Bar],
    barRenderer: BarRenderer = BarRenderer.default(),
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
      renderBarChart(barRenderer)
    )
  }
}
