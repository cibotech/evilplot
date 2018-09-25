package com.cibo.evilplot.geometry

import com.cibo.evilplot.JSONUtils.minifyProperties
import com.cibo.evilplot.colors.Color
import com.cibo.evilplot.numeric.{Point, Point2d}
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import io.circe.{Decoder, Encoder, ObjectEncoder}

sealed trait Gradient2d
object Gradient2d {
  implicit val decoder: Decoder[Gradient2d] = deriveDecoder[Gradient2d]
  implicit val encoder: Encoder[Gradient2d] = deriveEncoder[Gradient2d]
}

case class GradientStop(offset: Double, color: Color)
object GradientStop {
  implicit val decoder: Decoder[GradientStop] = deriveDecoder[GradientStop]
  implicit val encoder: Encoder[GradientStop] = deriveEncoder[GradientStop]
}

case class LinearGradient(x0: Double, y0: Double,
                          x1: Double, y1: Double, stops: Seq[GradientStop]) extends Gradient2d

object LinearGradient {
  implicit val decoder: Decoder[LinearGradient] = deriveDecoder[LinearGradient]
  implicit val encoder: Encoder[LinearGradient] = deriveEncoder[LinearGradient]

  def leftToRight(ex: Extent, stops: Seq[GradientStop]): LinearGradient =
    LinearGradient(0, 0, x1 = ex.width, 0, stops)

  def topToBottom(ex: Extent, stops: Seq[GradientStop]): LinearGradient =
    LinearGradient(0, 0, 0, y1 = ex.height, stops)
}

case class RadialGradient(x0: Double, y0: Double, r0: Double,
                          x1: Double, y1: Double, stops: Seq[GradientStop]) extends Gradient2d

object RadialGradient {
  implicit val decoder: Decoder[RadialGradient] = deriveDecoder[RadialGradient]
  implicit val encoder: Encoder[RadialGradient] = deriveEncoder[RadialGradient]

  def withinExtent(extent: Extent, stops: Seq[GradientStop]): RadialGradient = {
    val radius = extent.height.min(extent.width) / 2

    RadialGradient(
      extent.width / 2,
      extent.height / 2,
      radius,
      extent.width / 2,
      extent.height / 2,
      stops
    )
  }
}
