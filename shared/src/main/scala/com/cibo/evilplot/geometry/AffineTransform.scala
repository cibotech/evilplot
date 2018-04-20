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

import com.cibo.evilplot.numeric.Point
import io.circe.{Decoder, Encoder}

final case class AffineTransform(
  scaleX: Double = 1,
  shearX: Double = 0,
  shearY: Double = 0,
  scaleY: Double = 1,
  shiftX: Double = 0,
  shiftY: Double = 0
) {

  def scale(x: Double = 1, y: Double = 1): AffineTransform =
    this.compose(AffineTransform.scale(x, y))

  def translate(dx: Double, dy: Double): AffineTransform =
    this.compose(AffineTransform.translate(dx, dy))

  /** Rotates the transformation by `theta` radians.  */
  def rotate(theta: Double): AffineTransform = this.compose(AffineTransform.rotate(theta))

  /** Rotates the transformation by `theta` degrees. */
  def rotateDegrees(degs: Double): AffineTransform = rotate(degs / 180 * Math.PI)

  def flipOverX: AffineTransform = scale(y = -1)

  def flipOverY: AffineTransform = scale(x = -1)

  def rotateClockwise: AffineTransform = rotateDegrees(-90)

  def rotateCounterClockwise: AffineTransform = rotateDegrees(90)

  def compose(trans: AffineTransform): AffineTransform = {
    val newScaleX = trans.scaleX * scaleX + trans.shearX * shearY
    val newShearX = trans.scaleX * shearX + trans.shearX * scaleY
    val newShiftX = trans.scaleX * shiftX + trans.shearX * shiftY + trans.shiftX
    val newShearY = trans.shearY * scaleX + trans.scaleY * shearY
    val newScaleY = trans.shearY * shearX + trans.scaleY * scaleY
    val newShiftY = trans.shearY * shiftX + trans.scaleY * shiftY + trans.shiftY

    AffineTransform(newScaleX, newShearX, newShearY, newScaleY, newShiftX, newShiftY)
  }

  def apply(x: Double, y: Double): (Double, Double) = {
    val tx = x * scaleX + y * shearX + shiftX
    val ty = x * shearY + y * scaleY + shiftY
    (tx, ty)
  }

  def apply(p: Point): Point = Point.tupled(this.apply(p.x, p.y))
}

object AffineTransform {

  implicit val encoder: Encoder[AffineTransform] =
    io.circe.generic.semiauto.deriveEncoder[AffineTransform]
  implicit val decoder: Decoder[AffineTransform] =
    io.circe.generic.semiauto.deriveDecoder[AffineTransform]

  def identity: AffineTransform = AffineTransform()

  private def scale(sx: Double, sy: Double): AffineTransform =
    AffineTransform(scaleX = sx, scaleY = sy)

  private def translate(dx: Double, dy: Double): AffineTransform =
    AffineTransform(shiftX = dx, shiftY = dy)

  private def rotate(theta: Double): AffineTransform =
    AffineTransform(
      scaleX = Math.cos(theta),
      shearX = -Math.sin(theta),
      shearY = Math.sin(theta),
      scaleY = Math.cos(theta))
}
