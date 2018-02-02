/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.numeric

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._

// TODO:  reporting draws all the points atop the box plot. For now, this definition includes all the points.
// For general use, it doesn't make much sense to do this. (For the same reason it doesn't make sense to for the
// histogram).

case class BoxPlotSummaryStatistics(min: Double,
                                    max: Double,
                                    lowerWhisker: Double,
                                    upperWhisker: Double,
                                    lowerQuantile: Double,
                                    middleQuantile: Double,
                                    upperQuantile: Double,
                                    outliers: Seq[Double],
                                    allPoints: Seq[Double]) {
  override def toString: String = {
    s"""lowerWhisker $lowerWhisker lowerQuantile $lowerQuantile middleQuantile
       |$middleQuantile upperQuantile $upperQuantile upperWhisker $upperWhisker
     """.stripMargin
  }
}

/* This linearly interpolates to compute quantiles (which is the default behavior in NumPy and R)
 * see https://docs.scipy.org/doc/numpy-dev/reference/generated/numpy.percentile.html under `interpolation='linear'`
 */
object BoxPlotSummaryStatistics {

  implicit val encoder: Encoder[BoxPlotSummaryStatistics] = deriveEncoder[BoxPlotSummaryStatistics]
  implicit val decoder: Decoder[BoxPlotSummaryStatistics] = deriveDecoder[BoxPlotSummaryStatistics]

  def apply(data: Seq[Double],
            quantiles: (Double, Double, Double) = (0.25, 0.50, 0.75),
            includeAllPoints: Boolean = false): BoxPlotSummaryStatistics = {
    val sorted = data.sorted

    require(quantiles._1 < quantiles._2 && quantiles._2 < quantiles._3,
            "Supplied quantiles must be strictly increasing")
    val (lowerQuantile, middleQuantile, upperQuantile) =
      quantile(data, quantiles match { case (l, m, u) => Seq(l, m, u) }) match {
        case Seq(l, m, u) => (l, m, u)
      }

    // Summary stats
    val min: Double = sorted.head
    val max: Double = sorted.last

    val interQuartileRange = upperQuantile - lowerQuantile
    val lowerWhiskerLimit = lowerQuantile - 1.5 * interQuartileRange
    val upperWhiskerLimit = upperQuantile + 1.5 * interQuartileRange

    val outliersLeft =
      if (min < lowerWhiskerLimit) sorted.takeWhile(_ < lowerWhiskerLimit)
      else Nil
    val outliersRight =
      if (max > upperWhiskerLimit) sorted.dropWhile(_ < upperWhiskerLimit)
      else Nil

    val outliers: Seq[Double] = outliersLeft ++ outliersRight
    val lowerWhisker: Double = math.max(lowerWhiskerLimit, min)
    val upperWhisker: Double = math.min(upperWhiskerLimit, max)

    BoxPlotSummaryStatistics(min,
                             max,
                             lowerWhisker,
                             upperWhisker,
                             lowerQuantile,
                             middleQuantile,
                             upperQuantile,
                             outliers,
                             data)
  }
}
