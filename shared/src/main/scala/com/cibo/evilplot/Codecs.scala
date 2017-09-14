package com.cibo.evilplot
import com.cibo.evilplot.colors.HSL
import com.cibo.evilplot.numeric.{Bounds, Point}
import com.cibo.evilplot.plotdefs.{FacetsDef, PlotOptions, ScatterPlotDef}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
object Codecs {
  implicit val decodeHSL: Decoder[HSL] = deriveDecoder[HSL]
  implicit val encodeHSL: Encoder[HSL] = deriveEncoder[HSL]

  implicit val decodeScatter: Decoder[ScatterPlotDef] = deriveDecoder[ScatterPlotDef]
  implicit val encodeScatter: Encoder[ScatterPlotDef] = deriveEncoder[ScatterPlotDef]

  implicit val decodePlotOptions: Decoder[PlotOptions] = deriveDecoder[PlotOptions]
  implicit val encodePlotOptions: Encoder[PlotOptions] = deriveEncoder[PlotOptions]

  implicit val decodeFacetsDef: Decoder[FacetsDef] = deriveDecoder[FacetsDef]
  implicit val encodeFacetsDef: Encoder[FacetsDef] = deriveEncoder[FacetsDef]

  implicit val decodePoint: Decoder[Point] = deriveDecoder[Point]
  implicit val encodePoint: Encoder[Point] = deriveEncoder[Point]

  implicit val decodeBounds: Decoder[Bounds] = deriveDecoder[Bounds]
  implicit val encodeBounds: Encoder[Bounds] = deriveEncoder[Bounds]

}
