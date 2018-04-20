/*
 * Copyright (c) 2018, CiBO Technologies, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.cibo.evilplot.numeric

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._

case class BoxPlotSummaryStatistics(
  min: Double,
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

  def apply(
    data: Seq[Double],
    quantiles: (Double, Double, Double) = (0.25, 0.50, 0.75),
    includeAllPoints: Boolean = false): BoxPlotSummaryStatistics = {
    val sorted = data.sorted

    require(
      quantiles._1 < quantiles._2 && quantiles._2 < quantiles._3,
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

    BoxPlotSummaryStatistics(
      min,
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
