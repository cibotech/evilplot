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

package com.cibo.evilplot.geometry

import com.cibo.evilplot.JSONUtils.minifyProperties
import com.cibo.evilplot.colors.Color
import com.cibo.evilplot.numeric.{Point, Point2d}
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import io.circe.{Decoder, Encoder}

sealed trait Gradient2d {
  val stops: Seq[GradientStop]
}
object Gradient2d {
  implicit val decoder: Decoder[Gradient2d] = deriveConfiguredDecoder[Gradient2d]
  implicit val encoder: Encoder[Gradient2d] = deriveConfiguredEncoder[Gradient2d]
}

case class GradientStop(offset: Double, color: Color)
object GradientStop {
  implicit val decoder: Decoder[GradientStop] = deriveConfiguredDecoder[GradientStop]
  implicit val encoder: Encoder[GradientStop] = deriveConfiguredEncoder[GradientStop]
}

case class LinearGradient(x0: Double, y0: Double, x1: Double, y1: Double, stops: Seq[GradientStop])
    extends Gradient2d

object LinearGradient {
  implicit val decoder: Decoder[LinearGradient] = deriveConfiguredDecoder[LinearGradient]
  implicit val encoder: Encoder[LinearGradient] = deriveConfiguredEncoder[LinearGradient]

  def leftToRight(ex: Extent, stops: Seq[GradientStop]): LinearGradient =
    LinearGradient(0, 0, x1 = ex.width, 0, stops)

  def rightToLeft(ex: Extent, stops: Seq[GradientStop]): LinearGradient =
    LinearGradient(x0 = ex.width, 0, 0, 0, stops)

  def topToBottom(ex: Extent, stops: Seq[GradientStop]): LinearGradient =
    LinearGradient(0, 0, 0, y1 = ex.height, stops)

  def bottomToTop(ex: Extent, stops: Seq[GradientStop]): LinearGradient = {
    LinearGradient(0, y0 = ex.height, 0, 0, stops)
  }
}

case class RadialGradient(
  x0: Double,
  y0: Double,
  r0: Double,
  x1: Double,
  y1: Double,
  stops: Seq[GradientStop])
    extends Gradient2d

object RadialGradient {
  implicit val decoder: Decoder[RadialGradient] = deriveConfiguredDecoder[RadialGradient]
  implicit val encoder: Encoder[RadialGradient] = deriveConfiguredEncoder[RadialGradient]

  def withinExtent(extent: Extent, stops: Seq[GradientStop]): RadialGradient = {
    val radius = extent.height.min(extent.width) / 2

    RadialGradient(
      extent.width / 2,
      extent.height / 2,
      radius,
      extent.width / 2,
      extent.height / 2,
      stops
    )
  }
}
