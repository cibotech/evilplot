package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.{Drawable, Extent}
import com.cibo.evilplot.numeric.{Bounds, Point}
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.renderers.{BarRenderer, PlotRenderer}

object Histogram {

  val defaultBinCount: Int = 20

  // Create binCount bins from the given data and xbounds.
  private def createBins(values: Seq[Double], xbounds: Bounds, binCount: Int): Seq[Point] = {
    val binWidth = xbounds.range / binCount
    val grouped = values.groupBy { value => math.min(((value - xbounds.min) / binWidth).toInt, binCount - 1) }
    (0 until binCount).flatMap { i =>
      grouped.get(i).map { vs =>
        val y = vs.size
        val x = i * binWidth + xbounds.min
        Point(x, y)
      }
    }
  }

  case class HistogramRenderer(
    data: Seq[Double],
    barRenderer: BarRenderer,
    binCount: Int,
    spacing: Double,
    boundBuffer: Double
  ) extends PlotRenderer {
    def render(plot: Plot, plotExtent: Extent)(implicit theme: Theme): Drawable = {
      val xtransformer = plot.xtransform(plot, plotExtent)
      val ytransformer = plot.ytransform(plot, plotExtent)

      // The x bounds might have changed here, which could lead to a different binning of the data. If that
      // happens, it's possible for us to exceed our boundary. Thus we have two options:
      //  1. Clip at the boundary
      //  2. Scale all bars to have the correct relative heights.
      // Scaling the bars would show the correct histogram as long as no axis is displayed.  However, if
      // an axis is display, we would end up showing the wrong values. Thus, we clip if the y boundary is
      // fixed, otherwise we scale to make it look pretty.
      val points = createBins(data, plot.xbounds, binCount)
      val maxY = points.maxBy(_.y).y * (1.0 + boundBuffer)
      val yscale = if (plot.yfixed) 1.0 else math.min(1.0, plot.ybounds.max / maxY)

      val binWidth = plot.xbounds.range / binCount
      val yintercept = ytransformer(0)
      points.map { point =>
        val x = xtransformer(point.x) + spacing / 2.0
        val clippedY = math.min(point.y * yscale, plot.ybounds.max)
        val y = ytransformer(clippedY)
        val barWidth = math.max(xtransformer(point.x + binWidth) - x - spacing, 0)
        val bar = Bar(clippedY)
        val barHeight = yintercept - y
        barRenderer.render(plot, Extent(barWidth, barHeight), bar).translate(x = x, y = y)
      }.group
    }
  }

  /** Create a histogram.
    * @param values The data.
    * @param bins The number of bins to divide the data into.
    * @param barRenderer The renderer to render bars for each bin.
    * @param spacing The spacing between bars.
    * @param boundBuffer Extra padding to place at the top of the plot.
    * @return A histogram plot.
    */
  def apply(
    values: Seq[Double],
    bins: Int = defaultBinCount,
    barRenderer: Option[BarRenderer] = None,
    spacing: Option[Double] = None,
    boundBuffer: Double = BarChart.defaultBoundBuffer
  )(implicit theme: Theme): Plot = {
    require(bins > 0, "must have at least one bin")
    val xbounds = Bounds(values.min, values.max)
    val maxY = createBins(values, xbounds, bins).maxBy(_.y).y
    val binWidth = xbounds.range / bins
    Plot(
      xbounds = xbounds,
      ybounds = Bounds(0, maxY * (1.0 + boundBuffer)),
      renderer = HistogramRenderer(
        values,
        barRenderer.getOrElse(BarRenderer.default(theme.colors.bar)),
        bins,
        spacing.getOrElse(theme.elements.barSpacing),
        boundBuffer
      )
    )
  }
}
