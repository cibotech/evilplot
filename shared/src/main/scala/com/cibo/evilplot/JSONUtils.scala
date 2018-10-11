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

import io.circe.generic.extras.Configuration
import io.circe.parser.decode
import io.circe.syntax._
import io.circe.{Decoder, Encoder, Error}

object JSONUtils {
  // This should only be used for colors, drawables, and extents.
  private[evilplot] implicit val minifyProperties: Configuration = Configuration.default.copy(
    transformMemberNames = s => s.take(2).toString,
    transformConstructorNames = shortenedName
  )

  // scalastyle:off
  private def shortenedName(s: String): String = {
    s match {
      case "EmptyDrawable" => "E"
      case "Line"          => "L"
      case "Path"          => "P"
      case "Polygon"       => "p"
      case "Rect"          => "R"
      case "BorderRect"    => "B"
      case "Disc"          => "D"
      case "Wedge"         => "W"
      case "Translate"     => "T"
      case "Affine"        => "A"
      case "Scale"         => "C"
      case "Rotate"        => "O"
      case "Group"         => "G"
      case "Resize"        => "Re"
      case "Style"         => "S"
      case "StrokeStyle"   => "Y"
      case "StrokeWeight"  => "H"
      case "LineDash"      => "l"
      case "Text"          => "X"
      case "HSLA"          => "c"
      case "GradientFill"  => "gf"
      case other           => other
    }
  }
  // scalastyle:on

  // Wrap the Circe JSON decode method and return just the desired type, not an Either.
  // If parsing returns an error, throw the error rather than returning it.
  def decodeStr[A: Decoder](input: String): A = {
    val a: Either[Error, A] = decode[A](input)
    a match {
      case Left(error)   => throw error
      case Right(result) => result
    }
  }

  // Encode the input object to JSON, then convert the JSON to a string and return it.
  def encodeObj[A: Encoder](input: A): String = {
    input.asJson.noSpaces
  }
}
