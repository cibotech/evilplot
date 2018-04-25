package com.cibo.evilplot.geometry

import com.cibo.evilplot.JSONUtils
import io.circe.{Decoder, Encoder}
import io.circe.generic.extras.Configuration

/** The stroke pattern of a line.
  * @param dashPattern A sequence containing distances between the solid portions of
  *   the line and the invisible portions. A single value will result in equal-sized opaque
  *   segments and gaps. An empty list uses a solid line. All values must be positive.
  */
final case class LineStyle(
  dashPattern: Seq[Double] = Seq.empty[Double],
  offset: Double = 0.0
) {
  require(dashPattern.forall(_ > 0), "A dash pattern must only contain positive values.")
}
object LineStyle {
  import io.circe.generic.extras.semiauto._
  private implicit val jsonConfig: Configuration = JSONUtils.minifyProperties
  implicit val lineStyleEncoder: Encoder[LineStyle] = deriveEncoder[LineStyle]
  implicit val lineStyleDecoder: Decoder[LineStyle] = deriveDecoder[LineStyle]

  val Solid = LineStyle()
  val Dotted: LineStyle = LineStyle(Seq(1, 2))
  val DashDot: LineStyle = LineStyle(Seq(6, 3, 1, 3))
  def evenlySpaced(dist: Double): LineStyle = LineStyle(Seq(dist))
}
