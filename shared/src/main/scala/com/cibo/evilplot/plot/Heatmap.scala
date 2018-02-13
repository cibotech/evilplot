package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.{Drawable, Extent}
import com.cibo.evilplot.numeric.Bounds
import com.cibo.evilplot.plot.renderers.{BarRenderer, PlotRenderer}

object Heatmap {

  private case class HeatmapRenderer(barRenderer: BarRenderer[Seq[Seq[Bar]]]) extends PlotRenderer[Seq[Seq[Bar]]] {
    def render(plot: Plot[Seq[Seq[Bar]]], plotExtent: Extent): Drawable = {
      val xtransformer = plot.xtransform(plot, plotExtent)
      val ytransformer = plot.ytransform(plot, plotExtent)
      val rowCount = plot.data.size

      plot.data.zipWithIndex.map { case (row, yIndex) =>
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
    mapRenderer: Seq[Seq[Bar]] => BarRenderer[Seq[Seq[Bar]]] = BarRenderer.temperature()
  ): Plot[Seq[Seq[Bar]]] = {
    val barRenderer = mapRenderer(bars)
    val xbounds = Bounds(0, bars.map(_.size).max)
    val ybounds = Bounds(0, bars.size)
    Plot[Seq[Seq[Bar]]](
      bars,
      xbounds = xbounds,
      ybounds = ybounds,
      xfixed = true,
      yfixed = true,
      renderer = HeatmapRenderer(barRenderer)
    )
  }

  def apply(
    data: Seq[Seq[Double]],
    mapRenderer: Seq[Seq[Bar]] => BarRenderer[Seq[Seq[Bar]]] = BarRenderer.temperature()
  ): Plot[Seq[Seq[Bar]]] = barHeatmap(data.map(_.map(Bar.apply)), mapRenderer)

}
