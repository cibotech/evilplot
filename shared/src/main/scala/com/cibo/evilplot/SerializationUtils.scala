/*
 * Copyright 2017 CiBO Technologies
 */

package com.cibo.evilplot

import com.cibo.evilplot.colors.{Color, HSL, NamedColor, Clear}
import io.circe.{Decoder, Encoder, HCursor, Json}
import io.circe.generic.extras.Configuration
import io.circe.syntax._

// This comes from Travis Brown's answer on serializing ADTs.
// https://stackoverflow.com/questions/42165460/how-to-decode-an-adt-with-circe-without-disambiguating-objects
// it *does* add an additional field to the JSON with the case class constructor name.

object SerializationUtils {
  implicit val withCaseClassConstructorName: Configuration = Configuration.default.withDiscriminator("what_am_i")

  implicit object NamedColorEncoder extends Encoder[NamedColor] {
    override def apply(color: NamedColor): Json = Json.obj(("NamedColor", Json.fromString(color.repr)))
  }
  implicit object NamedColorDecoder extends Decoder[NamedColor] {
    override def apply(c: HCursor): Decoder.Result[NamedColor] = {
      for {
        color <- c.downField("NamedColor").as[String]
      } yield new NamedColor { val repr = color }
    }
  }

  implicit object ColorEncoder extends Encoder[Color] {
    override def apply(color: Color): Json = color match {
      case HSL(h, s, l) =>
        Json.obj(("HSL",
          Json.obj(("hue", Json.fromInt(h)), ("saturation", Json.fromInt(s)), ("lightness", Json.fromInt(l)))))
      case Clear => Json.obj(("Clear", Json.fromString("Clear")))
      case c: NamedColor => c.asJson
    }
  }
  // TODO. This is not exactly type safe. You can encode the color Red, which itself an object extending NamedColor,
  // and currently decoding it will only give you an anonymous class instance extending NamedColor with repr "red".
  // The produced visual is the same. The obvious workaround would be to disambiguate all 140 named colors. Meh.
  // (Circe's automatic derivation does not work when there are so many types, compiling will cause a stack overflow,
  // even with a massive stack size given to the compiler).
//  implicit object ColorDecoder extends Decoder[Color] {
//    override def apply(c: HCursor): Decoder.Result[Color] = {
//      val x = c.downField("NamedColor") match
//    }
//  }


}
