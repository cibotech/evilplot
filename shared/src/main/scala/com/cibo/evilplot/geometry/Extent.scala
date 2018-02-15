package com.cibo.evilplot.geometry

import com.cibo.evilplot.JSONUtils
import io.circe.generic.extras.Configuration
import io.circe.{Decoder, Encoder}

/**
  * Extent defines an object's rectangular bounding box.
  * As discussed in <a href="http://ozark.hendrix.edu/~yorgey/pub/monoid-pearl.pdf">
  * "Monoids: Theme and Variations" by Yorgey</a>,
  * rectangular bounding boxes don't play well with rotation.
  * We'll eventually need something fancier like the convex hull.
  *
  * @param width bounding box width
  * @param height bounding box height
  */
case class Extent(width: Double, height: Double) {
  def *(scale: Double): Extent = Extent(scale * width, scale * height)
  def -(w: Double = 0.0, h: Double = 0.0): Extent = Extent(width - w, height - h)
}

object Extent {
  private implicit val cfg: Configuration = JSONUtils.minifyProperties
  implicit val extentEncoder: Encoder[Extent] = io.circe.generic.extras.semiauto.deriveEncoder[Extent]
  implicit val extentDecoder: Decoder[Extent] = io.circe.generic.extras.semiauto.deriveDecoder[Extent]
}
