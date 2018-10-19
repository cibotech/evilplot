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

import scala.language.implicitConversions
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._

trait Point2d {
  val x: Double
  val y: Double
  def withXY(x: Double = this.x, y: Double = this.y): Point2d
}

case class Point3d[Z: Numeric](x: Double, y: Double, z: Z) extends Datum2d[Point3d[Z]] {
  def withXY(x: Double, y: Double): Point3d[Z] = this.copy(x, y, z)
}

trait Datum2d[A <: Datum2d[A]] extends Point2d {
  val x: Double
  val y: Double
  def withXY(x: Double = this.x, y: Double = this.y): A
}

final case class Point(x: Double, y: Double) extends Datum2d[Point] {
  def -(that: Point): Point = Point(x - that.x, y - that.y)

  def withXY(x: Double = this.x, y: Double = this.y): Point = this.copy(x = x, y = y)
}

object Point {
  implicit val encoder: Encoder[Point] = io.circe.generic.semiauto.deriveEncoder[Point]
  implicit val decoder: Decoder[Point] = io.circe.generic.semiauto.deriveDecoder[Point]
  def tupled(t: (Double, Double)): Point = Point(t._1, t._2)
  implicit def toTuple(p: Point): (Double, Double) = (p.x, p.y)
}

final case class Point3(x: Double, y: Double, z: Double)

object Point3 {
  def tupled(t: (Double, Double, Double)): Point3 = Point3(t._1, t._2, t._3)
}
