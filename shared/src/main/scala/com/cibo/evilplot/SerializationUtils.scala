/*
 * Copyright 2017 CiBO Technologies
 */

package com.cibo.evilplot

import com.cibo.evilplot.numeric.Histogram
import io.circe.{Decoder, Encoder, Error, HCursor, Json}
import io.circe.generic.extras.Configuration
import io.circe.syntax._
import io.circe.parser.decode

// This comes from Travis Brown's answer on serializing ADTs.
// https://stackoverflow.com/questions/42165460/how-to-decode-an-adt-with-circe-without-disambiguating-objects
// it *does* add an additional field to the JSON with the case class constructor name.

object SerializationUtils {
  implicit val withCaseClassConstructorName: Configuration = Configuration.default.withDiscriminator("what_am_i")

  // These come from the  repo.

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
  // TODO: Fix the decoder.
/*  implicit val encodeHistogram: Encoder[Histogram] = new Encoder[Histogram] {
    final def apply(h: Histogram): Json = Json.obj(
      "bins" -> Json.arr(h.bins.map(Json.fromLong): _*),
      "numBins" -> Json.fromInt(h.numBins),
      "binWidth" -> Json.fromDoubleOrNull(h.binWidth),
      "min" -> Json.fromDoubleOrNull(h.min),
      "max" -> Json.fromDoubleOrNull(h.max)
    )
  }

  implicit val decodeHistogram: Decoder[Histogram] = new Decoder[Histogram] {
    final def apply(c: HCursor): Decoder.Result[Histogram] = for {
      bins <- c.downField("bins").as[Seq[Long]]
      numBins <- c.downField("numBins").as[Int]
      binWidth <- c.downField("binWidth").as[Double]
      min <- c.downField("min").as[Double]
      max <- c.downField("max").as[Double]
    } yield Histogram(bins, numBins, binWidth, min, max, Nil)
  }*/
}
