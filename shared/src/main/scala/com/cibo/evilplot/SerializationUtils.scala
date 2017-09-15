package com.cibo.evilplot

import io.circe.generic.extras.Configuration

// This comes from Travis Brown's answer on serializing ADTs.
// https://stackoverflow.com/questions/42165460/how-to-decode-an-adt-with-circe-without-disambiguating-objects
// it *does* add an additional field to the JSON with the case class constructor name.

object SerializationUtils {
  implicit val withCaseClassConstructorName: Configuration = Configuration.default.withDiscriminator("what_am_i")
}
