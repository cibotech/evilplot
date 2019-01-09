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

final case class Bounds(min: Double, max: Double) {
  if (!min.isNaN && !max.isNaN) {
    require(min <= max, s"Bounds min must be <= max, $min !<= $max")
  }

  /*absolute range (since min <= max)*/
  lazy val range: Double = max - min

  lazy val midpoint: Double = (max + min) / 2.0

  def isInBounds(x: Double): Boolean = x >= min && x <= max

  /**if it exists find the intersection between two bounds*/
  def intersect(that: Bounds): Option[Bounds] = {
    val min = math.max(this.min, that.min)
    val max = math.min(this.max, that.max)
    if (min <= max) Some(Bounds(min, max)) else None
  }

  def union(that: Bounds): Bounds =
    Bounds(math.min(this.min, that.min), math.max(this.max, that.max))

  /**grow the bound by a specific amount
    * @param p ratio of the range to lower the min raise the max (note a negative value shrinks the bound)*/
  def pad(p: Double): Bounds = Bounds(min - range * p, max + range * p)
  def padMax(p: Double): Bounds = Bounds(min, max + range * p)
  def padMin(p: Double): Bounds = Bounds(min - range * p, max)
}

object Bounds {

  implicit val encoder: Encoder[Bounds] = deriveEncoder[Bounds]
  implicit val decoder: Decoder[Bounds] = deriveDecoder[Bounds]

  private def lift[T](expr: => T): Option[T] = {
    try {
      Some(expr)
    } catch {
      case _: Exception => None
    }
  }

  def union(bounds: Seq[Bounds]): Bounds = bounds reduce { _ union _ }

  def getBy[T](data: Seq[T])(f: T => Double): Option[Bounds] = {
    val mapped = data.map(f).filterNot(_.isNaN)
    for {
      min <- lift(mapped.min)
      max <- lift(mapped.max)
    } yield Bounds(min, max)
  }

  def of(data: Seq[Double]): Bounds = Bounds.get(data) getOrElse Bounds.empty

  def get(data: Seq[Double]): Option[Bounds] = {
    data.foldLeft(None: Option[Bounds]) { (bounds, value) =>
      bounds match {
        case None => Some(Bounds(value, value))
        case Some(Bounds(min, max)) =>
          Some(Bounds(math.min(min, value), math.max(max, value)))
      }
    }
  }
  def empty: Bounds = Bounds(0d, 0d)

  def widest(bounds: Seq[Option[Bounds]]): Option[Bounds] =
    bounds.flatten.foldLeft(None: Option[Bounds]) { (acc, curr) =>
      if (acc.isEmpty) Some(curr)
      else
        Some(Bounds(math.min(acc.get.min, curr.min), math.max(acc.get.max, curr.max)))
    }
}
