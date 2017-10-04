/*
 * Copyright 2017 CiBO Technologies
 */

package com.cibo.evilplot

import io.circe.parser.decode
import io.circe.syntax._
import io.circe.{Decoder, Encoder, Error}

object JSONUtils {
  // These come from the  repo. Move into common lib?

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
