/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.numeric

/* This class linearly interpolates to compute quantiles (which is the default behavior in NumPy and R)
 * see https://docs.scipy.org/doc/numpy-dev/reference/generated/numpy.percentile.html under `interpolation='linear'`
 */
class BoxPlot(data: Seq[Double], quantiles: (Double, Double, Double) = (0.25, 0.50, 0.75)) {
  private val sorted = data.sorted
  private val length = sorted.length

  private def getQuantile(quantile: Double) = {
    require(quantile >= 0.0 && quantile <= 1.0)
    val index: Double = quantile * (length - 1)
    if (index >= length - 1) sorted.last
    else {
      val lower = sorted(math.floor(index).toInt)
      val upper = sorted(math.ceil(index).toInt)
      lower + (upper - lower) * (index - math.floor(index))
    }
  }

  private val (lower, middle, upper) = quantiles
  require(lower < middle && middle < upper, "Supplied quantiles must be strictly increasing.")
  // Summary stats
  val min: Double = sorted.head
  val max: Double = sorted.last
  val lowerQuantile: Double = getQuantile(lower)
  val middleQuantile: Double = getQuantile(middle)
  val upperQuantile: Double = getQuantile(upper)


  private val interQuartileRange = upperQuantile - lowerQuantile
  private val lowerWhiskerLimit = lowerQuantile - 1.5 * interQuartileRange
  private val upperWhiskerLimit = upperQuantile + 1.5 * interQuartileRange

  private val outliersLeft = if (min < lowerWhiskerLimit) sorted.takeWhile(_ < lowerWhiskerLimit) else Nil
  private val outliersRight = if (max > upperWhiskerLimit) sorted.dropWhile(_ < upperWhiskerLimit) else Nil

  val outliers: Seq[Double] = outliersLeft ++ outliersRight
  val lowerWhisker: Double = math.max(lowerWhiskerLimit, min)
  val upperWhisker: Double = math.min(upperWhiskerLimit, max)
}
