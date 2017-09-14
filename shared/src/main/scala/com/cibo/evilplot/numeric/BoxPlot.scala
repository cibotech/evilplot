/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.numeric

/* This class linearly interpolates to compute quantiles (which is the default behavior in NumPy and R)
 * see https://docs.scipy.org/doc/numpy-dev/reference/generated/numpy.percentile.html under `interpolation='linear'`
 */
class BoxPlot(data: Seq[Double], quantiles: (Double, Double, Double) = (0.25, 0.50, 0.75)) {
  private val sorted = data.sorted

  require(quantiles._1 < quantiles._2 && quantiles._2 < quantiles._3, "Supplied quantiles must be strictly increasing")
  val (lowerQuantile, middleQuantile, upperQuantile) = quantile(data,
    quantiles match { case (l, m, u) => Seq(l, m, u) }) match { case Seq(l, m, u) => (l, m, u) }

  // Summary stats
  val min: Double = sorted.head
  val max: Double = sorted.last


  private val interQuartileRange = upperQuantile - lowerQuantile
  private val lowerWhiskerLimit = lowerQuantile - 1.5 * interQuartileRange
  private val upperWhiskerLimit = upperQuantile + 1.5 * interQuartileRange

  private val outliersLeft = if (min < lowerWhiskerLimit) sorted.takeWhile(_ < lowerWhiskerLimit) else Nil
  private val outliersRight = if (max > upperWhiskerLimit) sorted.dropWhile(_ < upperWhiskerLimit) else Nil

  val outliers: Seq[Double] = outliersLeft ++ outliersRight
  val lowerWhisker: Double = math.max(lowerWhiskerLimit, min)
  val upperWhisker: Double = math.min(upperWhiskerLimit, max)
}
