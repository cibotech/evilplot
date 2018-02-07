package com.cibo.evilplot.plot

import com.cibo.evilplot.plot.renderers.BarRenderer

object Histogram {

  val defaultBinCount: Int = 20

  def apply(
    values: Seq[Double],
    bins: Int = defaultBinCount,
    barRenderer: BarRenderer = BarRenderer.default(),
    spacing: Double = BarChart.defaultSpacing,
    boundBuffer: Double = BarChart.defaultBoundBuffer
  ): Plot[Seq[Bar]] = {
    require(bins > 0, "must have at least one bin")
    val (minValue, maxValue) = (values.min, values.max)
    val binWidth = (maxValue - minValue) / bins
    val grouped = values.groupBy { value => math.min(((value - minValue) / binWidth).toInt, bins - 1) }
    val bars = (0 until bins).map { i => Bar(grouped.get(i).map(_.size).getOrElse(0)) }
    BarChart(
      bars = bars,
      barRenderer = barRenderer,
      spacing = spacing,
      boundBuffer = boundBuffer
    ).xbounds(minValue, maxValue)
  }
}
