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

package com.cibo.evilplot.colors

private[colors] object GradientUtils {
  import ColorUtils.{interpolate, rgba}

  // Create a gradient of an arbitrary number of colors.
  private[colors] def multiGradient(colors: Seq[Color], min: Double, max: Double): Double => Color = {
    require(colors.nonEmpty, "A gradient cannot be constructed on an empty sequence of colors.")
    val numGradients = colors.length - 1

    if (numGradients == 0) (_: Double) => colors.head
    else {
      val singleGradientExtent = (max - min) / numGradients
      val gradients: Seq[PartialFunction[Double, HSLA]] = Seq.tabulate(numGradients) { i =>
        val lower = min + i * singleGradientExtent
        singleGradient(lower - 1e-5, lower + singleGradientExtent + 1e-5, colors(i), colors(i + 1))
      }
      gradients.reduce(_ orElse _)
    }
  }

  //scalastyle:on
  // https://stackoverflow.com/questions/22607043/color-gradient-algorithm
  // This is the "wrong" way, simple linear interpolation.
  private[colors] def singleGradient(minValue: Double,
                                  maxValue: Double,
                                  startColor: Color,
                                  endColor: Color): PartialFunction[Double, HSLA] = {
    case d if d >= minValue && d <= maxValue =>
      val (r1, g1, b1, a1) = rgba(startColor)
      val (r2, g2, b2, a2) = rgba(endColor)
      val range = maxValue - minValue
      val interpolationCoefficient = (d - minValue) / range
      val r = (255 * interpolate(r1, r2, interpolationCoefficient)).toInt
      val g = (255 * interpolate(g1, g2, interpolationCoefficient)).toInt
      val b = (255 * interpolate(b1, b2, interpolationCoefficient)).toInt
      val a = interpolate(a1, a2, interpolationCoefficient)
      RGBA(r, g, b, a)
  }
}
