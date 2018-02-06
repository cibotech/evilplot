package com.cibo.evilplot.plot

object Histogram {

  val defaultBinCount: Int = 20

  def apply(
    values: Seq[Double],
    bins: Int = defaultBinCount,
    barRenderer: BarRenderer = BarRenderer.default()
  ): Plot[Seq[Bar]] = {
    require(bins > 0, "must have at least one bin")
    val (minValue, maxValue) = (values.min, values.max)
    val binWidth = (maxValue - minValue) / bins
    val bars = (0 until bins).map { i =>
      val start = minValue + i * binWidth
      val end = start + binWidth
      val height = if (i + 1 < bins) values.count(v => v >= start && v < end) else values.count(_ >= start)
      Bar(height)
    }
    BarChart(bars).xbounds(minValue, maxValue)
  }
}
