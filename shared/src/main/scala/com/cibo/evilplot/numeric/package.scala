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

package com.cibo.evilplot

import scala.language.implicitConversions
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._

package object numeric {
  type Grid = Vector[Vector[Double]]

  final case class GridData(
    grid: Grid,
    xBounds: Bounds,
    yBounds: Bounds,
    zBounds: Bounds,
    xSpacing: Double,
    ySpacing: Double)

  private val normalConstant = 1.0 / math.sqrt(2 * math.Pi)

  // with sigma = 1.0 and mu = 0, like R's dnorm.
  private[numeric] def probabilityDensityInNormal(x: Double): Double =
    normalConstant * math.exp(-math.pow(x, 2) / 2)

  // quantiles using linear interpolation.
  private[numeric] def quantile(data: Seq[Double], quantiles: Seq[Double]): Seq[Double] = {
    if (data.isEmpty) Seq.fill(quantiles.length)(Double.NaN)
    else {
      val length = data.length
      val sorted = data.sorted
      for {
        quantile <- quantiles
        _ = require(quantile >= 0.0 && quantile <= 1.0)
        index = quantile * (length - 1)
        result = {
          if (index >= length - 1) sorted.last
          else {
            val lower = sorted(math.floor(index).toInt)
            val upper = sorted(math.ceil(index).toInt)
            lower + (upper - lower) * (index - math.floor(index))
          }
        }
      } yield result
    }
  }

  private[numeric] def mean(data: Seq[Double]): Double = data.sum / data.length

  private[numeric] def variance(data: Seq[Double]): Double = {
    val _mean = mean(data)
    data.map(x => math.pow(x - _mean, 2)).sum / (data.length - 1)
  }

  private[numeric] def standardDeviation(data: Seq[Double]): Double = math.sqrt(variance(data))

}
