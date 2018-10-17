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

object GradientUtils {
  import ColorUtils.{interpolate, rgba}

  /** Create a gradient of an arbitrary number of colors.
    * @param colors the stops in the gradient
    * @param min the minimum of the range over which to create the gradient
    * @param max the maximum of the range over which to create the gradient
    * @param mode the [[GradientMode]]
    * @return a Double => Color that returns an interpolated color for doubles
    *         in [min, max] and the respective endpoints for all other doubles.
    */
  def multiGradient(
    colors: Seq[Color],
    min: Double,
    max: Double,
    mode: GradientMode
  ): Double => Color = {
    require(colors.nonEmpty, "A gradient cannot be constructed on an empty sequence of colors.")
    val numGradients = colors.length - 1

    if (numGradients == 0) (_: Double) => colors.head
    else {
      val singleGradientExtent = (max - min) / numGradients
      val gradients: Seq[PartialFunction[Double, Color]] = Seq.tabulate(numGradients) { i =>
        val lower = min + i * singleGradientExtent
        singleGradient(
          lower - 1e-5,
          lower + singleGradientExtent + 1e-5,
          colors(i),
          colors(i + 1),
          mode)
      }
      val complete = gradients.reduce(_ orElse _)
      (d: Double) =>
        {
          if (d < min) colors.head
          else if (d > max) colors.last
          else if (d.isNaN) Clear
          else complete(d)
        }
    }
  }

  def multiGradient(colors: Seq[Color], min: Double, max: Double): Double => Color =
    multiGradient(colors, min, max, GradientMode.Linear)

  /** Create a gradient between two colors.
    * @return A PartialFunction[Double, Color], only defined inside  [minValue, maxValue]
    *         If a function that is defined for all doubles is desired, use [[singleGradientComplete]] */
  def singleGradient(
    minValue: Double,
    maxValue: Double,
    startColor: Color,
    endColor: Color,
    mode: GradientMode
  ): PartialFunction[Double, Color] = {
    {
      case d if d >= minValue && d <= maxValue =>
        import mode._
        val (r1, g1, b1, a1) = rgba(startColor)
        val (r2, g2, b2, a2) = rgba(endColor)
        val range = maxValue - minValue
        val interpolationCoefficient = (d - minValue) / range
        val r = interpolate(inverse(r1), inverse(r2), interpolationCoefficient)
        val g = interpolate(inverse(g1), inverse(g2), interpolationCoefficient)
        val b = interpolate(inverse(b1), inverse(b2), interpolationCoefficient)
        val a = interpolate(a1, a2, interpolationCoefficient)
        RGBA((255 * forward(r)).toInt, (255 * forward(g)).toInt, (255 * forward(b)).toInt, a)
      case noData if noData.isNaN => Clear
    }
  }

  /** Create a gradient between two colors on a double range that is defined outside of
    * the range.
    * @return a Double => Color that returns an interpolated color for doubles inside
    *        the range, or the respective end point for doubles outside the range. */
  def singleGradientComplete(
    minValue: Double,
    maxValue: Double,
    startColor: Color,
    endColor: Color,
    mode: GradientMode): Double => Color =
    multiGradient(Seq(startColor, endColor), minValue, maxValue, mode)

}
