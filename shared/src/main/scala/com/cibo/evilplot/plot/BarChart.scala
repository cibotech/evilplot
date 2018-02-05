package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.{Drawable, Extent}
import com.cibo.evilplot.numeric.Bounds

object BarChart {

  /*
  private def renderBarChart(barRenderer: BarRenderer)(plot: Plot[Seq[Bar]], plotExtent: Extent): Drawable = {
    val xtransformer = plot.xtransform(plot, plotExtent)
    val ytransformer = plot.ytransform(plot, plotExtent)
  }

  def apply(
    bars: Seq[Bar],
    barRenderer: BarRenderer
  ): Plot[Seq[Bar]] = {
    val xbounds = Bounds(0, bars.size)
    val ybounds = Bounds(bars.minBy(_.height).height, bars.maxBy(_.height).height)
    Plot[Seq[Bar]](
      bars,
      xbounds,
      ybounds,
      renderBarChart(barRenderer)
    )
  }
  */

}
