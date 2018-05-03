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

import io.circe.{Decoder, Encoder}

sealed trait Color {
  val repr: String
}

case object Clear extends Color {
  val repr = "hsla(0, 0%, 0%, 0)"
}

case class HSLA(hue: Int, saturation: Int, lightness: Int, opacity: Double) extends Color {
  require(hue >= 0 && hue < 360, s"hue must be within [0, 360) {was $hue}")
  require(
    saturation >= 0 && saturation <= 100,
    s"saturation must be within [0, 100] {was $saturation}")
  require(lightness >= 0 && lightness <= 100, s"lightness must be within [0, 100] {was $lightness}")
  require(opacity >= 0 && opacity <= 1.0, s"transparency must be within [0, 1.0] {was $opacity}")

  private def boundHue(hue: Int) = if (hue < 0) hue + 360 else if (hue >= 360) hue - 360 else hue

  private def floorCeiling(value: Int)(floor: Int, ceiling: Int) = value.min(ceiling).max(floor)

  def triadic: (HSLA, HSLA) = (
    this.copy(hue = boundHue(this.hue - 120)),
    this.copy(hue = boundHue(this.hue + 120))
  )

  def analogous(offsetDegrees: Int = 14): (HSLA, HSLA) = (
    this.copy(hue = boundHue(this.hue - offsetDegrees)),
    this.copy(hue = boundHue(this.hue + offsetDegrees))
  )

  def darken(percent: Int): HSLA = {
    val newLightness = floorCeiling(lightness - percent)(0, 100)
    this.copy(lightness = newLightness)
  }

  def lighten(percent: Int): HSLA = {
    val newLightness = floorCeiling(lightness + percent)(0, 100)
    this.copy(lightness = newLightness)
  }

  val repr = s"hsla($hue, $saturation%, $lightness%, $opacity)"
}

object RGBA {
  def apply(r: Int, g: Int, b: Int, a: Double): HSLA = ColorUtils.rgbaToHsla(r, g, b, a)
}

object RGB {
  def apply(r: Int, g: Int, b: Int): HSLA = ColorUtils.rgbaToHsla(r, g, b, 1.0)
}

object HSL {
  def apply(hue: Int, saturation: Int, lightness: Int): HSLA = HSLA(hue, saturation, lightness, 1.0)
}

object HEX {
  def apply(string: String): HSLA = ColorUtils.hexToHsla(string)
}

object Color {
  private implicit val cfg = com.cibo.evilplot.JSONUtils.minifyProperties
  implicit val encoder: Encoder[Color] = io.circe.generic.extras.semiauto.deriveEncoder[Color]
  implicit val decoder: Decoder[Color] = io.circe.generic.extras.semiauto.deriveDecoder[Color]

  def stream: Seq[Color] = {
    val hueSpan = 7
    Stream.from(0).map { i =>
      // if hueSpan = 8, for instance:
      // Epoch 0 ->  8 equally spaced  0 -  7
      // Epoch 1 -> 16 equally spaced  8 - 21
      // Epoch 2 -> 32 equally spaced 22 - 53
      // Epoch 3 -> 64 equally spaced 54 - 117
      // Epoch 4 -> 128 equally spaced 118 - 245
      // ..
      // e^2 * 8
      // pt = sum_epoch( 8 * 2 ^ (e) ) - log(8)/log(2) // not quite right this last term?
      // pt = 8 * 2 ^ (2) + 8 * 2 ^ (1) + 8 * 2 ^ (0) - 3
      // pt = 8 * (2 ^ (2) + 2 ^ (1) + 2 ^ (0)) - 3
      // pt = 8 * (2^(e+1) - 1) - 3

      import math._
      def log2(x: Double) = log(x) / log(2)
      // scalastyle:off
      val magicFactor = log2(hueSpan) // TODO: this may or may not be correct for other hueSpan's
      // scalastyle:on
      val epoch = if (i < hueSpan) 0 else ceil(log2(((i + magicFactor) / hueSpan) + 1) - 1).toInt

      def endIndexOfThisEpoch(e: Int) = 8 * (pow(2, e + 1) - 1) - magicFactor

      val slicesThisEpoch = hueSpan * Math.pow(2, epoch)
      val initialRotate = 360.0 / slicesThisEpoch / 2.0

      val zeroBasedIndexInThisEpoch = i - endIndexOfThisEpoch(epoch - 1) - 1

      val saturationDropoff = 2
      def saturationLevel(e: Int) = 100 * 1.0 / pow(saturationDropoff, epoch + 1)
      val saturationBase = 50 //100 - saturationLevel(0)
      HSL(
        abs(round(initialRotate + 360.0 / slicesThisEpoch * zeroBasedIndexInThisEpoch).toInt % 360),
        (saturationBase + saturationLevel(epoch)).round.toInt,
        50
      )
    }
  }

  def getGradientSeq(nColors: Int, startHue: Int = 0, endHue: Int = 359): Seq[Color] = {
    require(endHue > startHue, "End hue not greater than start hue")
    require(endHue <= 359, "End hue must be <= 359")
    val deltaH = (endHue - startHue) / nColors.toFloat
    val colors: Seq[Color] = Seq.tabulate(nColors)(x => HSL(startHue + (x * deltaH).toInt, 90, 54))
    colors
  }

  def getDefaultPaletteSeq(nColors: Int): Seq[Color] = {
    val stream = Stream.continually(DefaultColors.lightPalette.toStream)
    stream.flatten.take(nColors)
  }

  def getAnalogousSeq(seed: HSLA = HSL(207, 90, 54), depth: Int): Seq[Color] = {
    analogGrow(seed, depth)
  }

  def analogGrow(node: HSLA, depth: Int): Seq[Color] = {
    val left = node.analogous()._1
    val right = node.analogous()._2
    if (depth > 0) node +: (triadGrow(left, depth - 1) ++ triadGrow(right, depth - 1))
    else Seq()
  }

  def triadGrow(node: HSLA, depth: Int): Seq[Color] = {
    val left = node.triadic._1
    val right = node.triadic._2
    if (depth > 0) node +: (analogGrow(left, depth - 1) ++ analogGrow(right, depth - 1))
    else Seq()
  }
}
