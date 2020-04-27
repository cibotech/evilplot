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

object ColorUtils {

  //https://en.wikipedia.org/wiki/Hue
  //https://en.wikipedia.org/wiki/HSL_and_HSV

  private def calculateHueFromRGB(
    max: Double,
    min: Double,
    red: Double,
    green: Double,
    blue: Double
  ): Double = {
    if (max == min) {
      0.0
    } else {
      val chroma = max - min

      val hue = max match {
        case redHue if redHue == red       => (green - blue) / chroma + (if (green < blue) 6 else 0)
        case greenHue if greenHue == green => (blue - red) / chroma + 2
        case blueHue if blueHue == blue    => (red - green) / chroma + 4
      }

      hue / 6
    }
  }

  private def calculateSaturationFromMagnitude(max: Double, min: Double): Double = {
    if (max == min) {
      0.0
    } else {
      val chroma = max - min
      val lightness = (max + min) / 2

      chroma / (1 - Math.abs(2 * lightness - 1))
    }
  }

  def rgbaToHsla(r: Int, g: Int, b: Int, a: Double): HSLA = {

    require(r >= 0 && r < 256, s"r must be within [0, 256) {was $r}")
    require(g >= 0 && g < 256, s"g must be within [0, 256) {was $g}")
    require(b >= 0 && b < 256, s"b must be within [0, 256) {was $b}")

    val red = r / 255.0
    val green = g / 255.0
    val blue = b / 255.0

    val max = Seq(red, green, blue).max
    val min = Seq(red, green, blue).min

    val hueFraction = calculateHueFromRGB(max, min, red, green, blue)
    val saturationFraction = calculateSaturationFromMagnitude(max, min)
    val lightnessFraction = (max + min) / 2

    HSLA(
      hue = Math.round(hueFraction * 360).toInt % 360,
      saturation = Math.round(saturationFraction * 100).toInt,
      lightness = Math.round(lightnessFraction * 100).toInt,
      opacity = a
    )
  }

  def hexToHsla(hexString: String): HSLA = {

    require(hexString.length > 0, "hex string length <= 0")

    val hexValues = if (hexString.startsWith("#")) hexString.tail else hexString

    require(hexValues.length % 3 == 0 && hexValues.length <= 6, s"invalid hex string: $hexString")

    val split = hexValues.sliding(hexValues.length / 3, hexValues.length / 3)

    val Seq(r, g, b) = if (hexValues.length == 3) {
      split.map(Integer.parseInt(_, 16)).map(v => v + v * 16).toSeq
    } else {
      split.map(Integer.parseInt(_, 16)).toSeq
    }

    rgbaToHsla(r, g, b, 1.0)
  }

  private[colors] def interpolate(
    component1: Double,
    component2: Double,
    coefficient: Double
  ): Double = {
    component1 * (1 - coefficient) + coefficient * component2
  }

  // https://en.wikipedia.org/wiki/HSL_and_HSV#From_HSL
  // scalastyle:off
  def hslaToRgba(hsla: HSLA): (Double, Double, Double, Double) = {
    val l = hsla.lightness / 100.0
    val s = hsla.saturation / 100.0
    val c = (1d - math.abs(2d * l - 1d)) * s
    val hPrime = hsla.hue / 60d
    val x = c * (1d - math.abs((hPrime % 2) - 1d))
    val (r1, g1, b1): (Double, Double, Double) =
      if (hPrime.isNaN) (0, 0, 0)
      else if (hPrime >= 0 && hPrime < 1) (c, x, 0)
      else if (hPrime >= 1 && hPrime < 2) (x, c, 0)
      else if (hPrime >= 2 && hPrime < 3) (0, c, x)
      else if (hPrime >= 3 && hPrime < 4) (0, x, c)
      else if (hPrime >= 4 && hPrime < 5) (x, 0, c)
      else (c, 0, x)

    val m = l - .5 * c
    (r1 + m, g1 + m, b1 + m, hsla.opacity)
  }
  // scalastyle:on

  def rgba(c: Color): (Double, Double, Double, Double) = c match {
    case hsla: HSLA => hslaToRgba(hsla)
    case Clear      => (0, 0, 0, 0)
    case ClearWhite => (255, 255, 255, 0)
  }
}
