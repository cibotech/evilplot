/*
 * Copyright 2017 CiBO Technologies
 */

package com.cibo.evilplot

import io.circe.generic.extras.Configuration
import io.circe.parser.decode
import io.circe.syntax._
import io.circe.{Decoder, Encoder, Error}

object JSONUtils {
  // This should only be used for colors, drawables, and extents.
  private[evilplot] implicit val minifyProperties: Configuration = Configuration.default.copy(
    transformMemberNames = s => s.head.toString,
    transformConstructorNames = shortenedName
  )

  private def shortenedName(s: String): String = {
    s match {
      case "EmptyDrawable" => "E"
      case "Line" => "L"
      case "Path" => "P"
      case "Rect" => "R"
      case "BorderRect" => "B"
      case "Disc" => "D"
      case "Wedge" => "W"
      case "Translate" => "T"
      case "Affine" => "A"
      case "Scale" => "C"
      case "Rotate" => "O"
      case "UnsafeRotate" => "U"
      case "Group" => "G"
      case "Resize" => "Re"
      case "Style" => "S"
      case "StrokeStyle" => "Y"
      case "StrokeWeight" => "H"
      case "Text" => "X"
      case "HSLA" => "c"
      case other => other
    }
  }

  // Wrap the Circe JSON decode method and return just the desired type, not an Either.
  // If parsing returns an error, throw the error rather than returning it.
  def decodeStr[A: Decoder](input: String): A = {
    val a: Either[Error, A] = decode[A](input)
    a match {
      case Left(error) => throw error
      case Right(result) => result
    }
  }

  // Encode the input object to JSON, then convert the JSON to a string and return it.
  def encodeObj[A: Encoder](input: A): String = {
    input.asJson.noSpaces
  }
}
