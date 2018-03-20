package com.cibo.evilplot.plot.renderers

import com.cibo.evilplot.geometry.{Align, BorderRect, Drawable, Extent, Line, StrokeStyle}
import com.cibo.evilplot.numeric.BoxPlotSummaryStatistics
import com.cibo.evilplot.plot.Plot
import com.cibo.evilplot.plot.aesthetics.Theme

trait BoxRenderer extends PlotElementRenderer[BoxPlotSummaryStatistics] {
  def render(plot: Plot, extent: Extent, summary: BoxPlotSummaryStatistics): Drawable
}

object BoxRenderer {
  def default()(implicit theme: Theme): BoxRenderer = new BoxRenderer {
    def render(
      plot: Plot,
      extent: Extent,
      summary: BoxPlotSummaryStatistics
    ): Drawable = {
      val scale = extent.height / (summary.upperWhisker - summary.lowerWhisker)
      val topWhisker = summary.upperWhisker - summary.upperQuantile
      val uppperToMiddle = summary.upperQuantile - summary.middleQuantile
      val middleToLower = summary.middleQuantile - summary.lowerQuantile
      val bottomWhisker = summary.lowerQuantile - summary.lowerWhisker

      Align.center(
        StrokeStyle(Line(scale * topWhisker, theme.elements.strokeWidth), theme.colors.path).rotated(90),
        BorderRect.filled(extent.width, scale * uppperToMiddle).colored(theme.colors.path).filled(theme.colors.fill),
        BorderRect.filled(extent.width, scale * middleToLower).colored(theme.colors.path).filled(theme.colors.fill),
        StrokeStyle(Line(scale * bottomWhisker, theme.elements.strokeWidth), theme.colors.path).rotated(90)
      ).reduce(_ above _)
    }
  }
}
