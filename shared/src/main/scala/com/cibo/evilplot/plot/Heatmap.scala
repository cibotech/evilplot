package com.cibo.evilplot.plot

import com.cibo.evilplot.colors.{Color, ScaledColorBar}
import com.cibo.evilplot.geometry.{Drawable, Extent, Rect}
import com.cibo.evilplot.numeric.Bounds
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.renderers.PlotRenderer

object Heatmap {

  val defaultColorCount: Int = 10

  private case class HeatmapRenderer(
    data: Seq[Seq[Double]],
    colorBar: ScaledColorBar
  )(implicit theme: Theme) extends PlotRenderer {
    override def legendContext: LegendContext = LegendContext.fromColorBar(colorBar)
    def render(plot: Plot, plotExtent: Extent)(implicit theme: Theme): Drawable = {
      val xtransformer = plot.xtransform(plot, plotExtent)
      val ytransformer = plot.ytransform(plot, plotExtent)
      val rowCount = data.size

      data.zipWithIndex.map { case (row, yIndex) =>
        row.zipWithIndex.map { case (value, xIndex) =>
          val y = ytransformer(yIndex)
          val x = xtransformer(xIndex)
          val width = xtransformer(xIndex + 1) - x
          val height = ytransformer(yIndex + 1) - y
          Rect(width, height).filled(colorBar.getColor(value)).translate(x, y)
        }.group
      }.group
    }
  }

  /** Create a heatmap using a ScaledColorBar.
    * @param data The heatmap data.
    * @param colorBar The color bar to use.
    */
  def apply(
    data: Seq[Seq[Double]],
    colorBar: ScaledColorBar
  )(implicit theme: Theme): Plot = {
    val xbounds = Bounds(0, data.maxBy(_.size).size)
    val ybounds = Bounds(0, data.size)
    Plot(
      xbounds = xbounds,
      ybounds = ybounds,
      xfixed = true,
      yfixed = true,
      renderer = HeatmapRenderer(data, colorBar)
    )
  }

  /** Create a heatmap using a color sequence.
    * @param data The heatmap data.
    * @param colorCount The number of colors to use from the sequence.
    * @param colors The color sequence.
    */
  def apply(
    data: Seq[Seq[Double]],
    colorCount: Int = defaultColorCount,
    colors: Seq[Color] = Color.stream
  )(implicit theme: Theme): Plot = {
    val minValue = data.minBy(_.min).min
    val maxValue = data.maxBy(_.max).max
    val colorBar = ScaledColorBar(colors.take(colorCount), minValue, maxValue)
    apply(data, colorBar)
  }
}
