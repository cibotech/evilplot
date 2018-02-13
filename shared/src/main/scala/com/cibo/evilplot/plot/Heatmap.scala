package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.{Drawable, Extent}
import com.cibo.evilplot.numeric.Bounds
import com.cibo.evilplot.plot.renderers.{BarRenderer, PlotRenderer}

object Heatmap {

  private case class HeatmapRenderer(
    data: Seq[Seq[Bar]],
    barRenderer: BarRenderer
  ) extends PlotRenderer {
    def render(plot: Plot, plotExtent: Extent): Drawable = {
      val xtransformer = plot.xtransform(plot, plotExtent)
      val ytransformer = plot.ytransform(plot, plotExtent)
      val rowCount = data.size

      data.zipWithIndex.map { case (row, yIndex) =>
        row.zipWithIndex.map { case (bar, xIndex) =>
          val y = ytransformer(yIndex)
          val x = xtransformer(xIndex)
          val width = xtransformer(xIndex + 1) - x
          val height = ytransformer(yIndex + 1) - y
          val barExtent = Extent(width, height)
          barRenderer.render(plot, barExtent, bar).translate(x, y)
        }.group
      }.group
    }
  }

  def barHeatmap(
    bars: Seq[Seq[Bar]],
    mapRenderer: Seq[Seq[Bar]] => BarRenderer = BarRenderer.temperature()
  ): Plot = {
    val barRenderer = mapRenderer(bars)
    val xbounds = Bounds(0, bars.map(_.size).max)
    val ybounds = Bounds(0, bars.size)
    Plot(
      xbounds = xbounds,
      ybounds = ybounds,
      xfixed = true,
      yfixed = true,
      renderer = HeatmapRenderer(bars, barRenderer)
    )
  }

  def apply(
    data: Seq[Seq[Double]],
    mapRenderer: Seq[Seq[Bar]] => BarRenderer = BarRenderer.temperature()
  ): Plot = barHeatmap(data.map(_.map(Bar.apply)), mapRenderer)

}
