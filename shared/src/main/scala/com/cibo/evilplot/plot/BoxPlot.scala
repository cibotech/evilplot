package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry._
import com.cibo.evilplot.numeric.{Bounds, BoxPlotSummaryStatistics}
import com.cibo.evilplot.plot.renderers.{BarRenderer, BoxRenderer, PlotRenderer, PointRenderer}

private final case class BoxPlotRenderer(
                                        boxRenderer: BoxRenderer,
                                        pointRenderer: PointRenderer,
                                        spacing: Double
                                        ) extends PlotRenderer[Seq[BoxPlotSummaryStatistics]] {
  def render(plot: Plot[Seq[BoxPlotSummaryStatistics]], plotExtent: Extent): Drawable = {
    val ytransformer: Double => Double = plot.ytransform(plot, plotExtent)

    // Total box spacing used.
    val boxCount = plot.data.size
    val totalBoxSpacing = (boxCount - 1) * spacing

    // The width of each box.
    val boxWidth = (plotExtent.width - totalBoxSpacing) / boxCount

    plot.data.zipWithIndex.foldLeft(EmptyDrawable(): Drawable) { case (d, (summary, index)) =>
      val boxHeight = ytransformer(summary.lowerWhisker) - ytransformer(summary.upperWhisker)
      val box = boxRenderer.render(summary, Extent(boxWidth, boxHeight), index)
      println(box.extent.height, summary.upperWhisker - summary.lowerWhisker)
      val y = ytransformer(summary.upperWhisker)

      val points = summary.outliers
        .map(pt => pointRenderer.render(index).translate(x = boxWidth / 2 + (if (index == 0) 0 else spacing), y = ytransformer(pt)))
      d beside (box.translate(x = if (index == 0) 0 else spacing, y = y) behind points.group)
    }

//    val sorted = plot.data.sorted

//    val initial: (Double, Drawable) = (sorted.head.head, EmptyDrawable())
/*    sorted.zipWithIndex
      .foldLeft(initial) {
        case ((lastGroup, d), (bar, barIndex)) =>
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
          (bar.group,
           d beside barRenderer
             .render(bar, Extent(barWidth, barHeight), barIndex)
             .translate(y = y, x = x))
      }
      ._2*/
  }
}

object BoxPlot {
  private val defaultBoundBuffer: Double = 0.1
  private val defaultSpacing: Double = 20
  def apply(data: Seq[Seq[Double]],
            boxRenderer: BoxRenderer = BoxRenderer.default(),
            pointRenderer: PointRenderer = PointRenderer.default(),
            quantiles: (Double, Double, Double) = (0.25, 0.50, 0.75),
            spacing: Double = defaultSpacing,
            boundBuffer: Double = defaultBoundBuffer): Plot[Seq[BoxPlotSummaryStatistics]] = {
    val summaries = data.map(dist => BoxPlotSummaryStatistics(dist, quantiles))
    val xbounds = Bounds(0, summaries.size - 1)
    val ybounds = Plot.expandBounds(Bounds(summaries.minBy(_.min).min, summaries.maxBy(_.max).max), boundBuffer)
    Plot(summaries, xbounds, ybounds, BoxPlotRenderer(boxRenderer, pointRenderer, spacing))
  }

}
