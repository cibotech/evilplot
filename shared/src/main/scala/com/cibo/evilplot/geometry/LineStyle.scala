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

import com.cibo.evilplot.JSONUtils
import io.circe.{Decoder, Encoder}
import io.circe.generic.extras.Configuration

/** The stroke pattern of a line.
  * @param dashPattern A sequence containing distances between the solid portions of
  *   the line and the invisible portions. A single value will result in equal-sized opaque
  *   segments and gaps. An empty list uses a solid line. All values must be positive.
  * @param offset The "phase" of the dash pattern.
  */
final case class LineStyle(
  dashPattern: Seq[Double] = Seq.empty[Double],
  offset: Double = 0.0
) {
  require(dashPattern.forall(_ > 0), "A dash pattern must only contain positive values.")
}
object LineStyle {
  import io.circe.generic.extras.semiauto._
  private implicit val jsonConfig: Configuration = JSONUtils.minifyProperties
  implicit val lineStyleEncoder: Encoder[LineStyle] = deriveConfiguredEncoder[LineStyle]
  implicit val lineStyleDecoder: Decoder[LineStyle] = deriveConfiguredDecoder[LineStyle]

  val Solid: LineStyle = LineStyle()
  val Dotted: LineStyle = LineStyle(Seq(1, 2))
  val DashDot: LineStyle = LineStyle(Seq(6, 3, 1, 3))
  val Dashed: LineStyle = LineStyle(Seq(6))
  def evenlySpaced(dist: Double): LineStyle = LineStyle(Seq(dist))
}
