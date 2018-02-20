/*
 * Copyright 2017 CiBO Technologies
 */

package com.cibo.evilplot.geometry

import com.cibo.evilplot.JSONUtils.minifyProperties
import com.cibo.evilplot.colors.Color
import com.cibo.evilplot.numeric.{Point, Segment}
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import io.circe.{Decoder, Encoder}

/**
  * All Drawable objects define a draw method that draws to a 2D canvas, and a bounding box (Extent).
  * The bounding box must not change.
  */
sealed trait Drawable {
  def extent: Extent
  def draw(context: RenderContext): Unit

  private[evilplot] def isEmpty: Boolean = false
}

final case class EmptyDrawable() extends Drawable {
  val extent: Extent = Extent(0, 0)
  def draw(context: RenderContext): Unit = ()

  override private[evilplot] def isEmpty: Boolean = true
}
object EmptyDrawable {
  implicit val encoder: Encoder[EmptyDrawable] = deriveEncoder[EmptyDrawable]
  implicit val decoder: Decoder[EmptyDrawable] = deriveDecoder[EmptyDrawable]
}

final case class Line(length: Double, strokeWidth: Double) extends Drawable {
  lazy val extent = Extent(length, strokeWidth)
  def draw(context: RenderContext): Unit = context.draw(this)
}
object Line {
  implicit val encoder: Encoder[Line] = deriveEncoder[Line]
  implicit val decoder: Decoder[Line] = deriveDecoder[Line]
}

final case class Path(points: Seq[Point], strokeWidth: Double) extends Drawable {
  private lazy val xS: Seq[Double] = points.map(_.x)
  private lazy val yS: Seq[Double] = points.map(_.y)
  lazy val extent: Extent = if (points.nonEmpty) Extent(xS.max - xS.min, yS.max - yS.min) else Extent(0, 0)
  def draw(context: RenderContext): Unit = if (points.nonEmpty) context.draw(this) else ()
}
object Path {
  implicit val encoder: Encoder[Path] = deriveEncoder[Path]
  implicit val decoder: Decoder[Path] = deriveDecoder[Path]
  def apply(segment: Segment, strokeWidth: Double): Path = Path(Seq(segment.a, segment.b), strokeWidth)
}

final case class Rect(width: Double, height: Double) extends Drawable {
  lazy val extent: Extent = Extent(width, height)
  def draw(context: RenderContext): Unit = context.draw(this)
}
object Rect {
  implicit val encoder: Encoder[Rect] = deriveEncoder[Rect]
  implicit val decoder: Decoder[Rect] = deriveDecoder[Rect]

  def apply(side: Double): Rect = Rect(side, side)
  def apply(size: Extent): Rect = Rect(size.width, size.height)
}

final case class BorderRect(width: Double, height: Double) extends Drawable {
  lazy val extent: Extent = Extent(width, height)
  def draw(context: RenderContext): Unit = context.draw(this)
}
object BorderRect {
  implicit val encoder: Encoder[BorderRect] = deriveEncoder[BorderRect]
  implicit val decoder: Decoder[BorderRect] = deriveDecoder[BorderRect]

  def filled(width: Double, height: Double): Drawable = {
    Seq(Rect(width, height), BorderRect(width, height)).group
  }
}

final case class Disc(radius: Double, x: Double = 0, y: Double = 0) extends Drawable {
  require(x >= 0 && y >=0, s"x {$x} and y {$y} must both be positive")
  lazy val extent = Extent(x + radius, y + radius)

  def draw(context: RenderContext): Unit = context.draw(this)
}
object Disc {
  implicit val encoder: Encoder[Disc] = deriveEncoder[Disc]
  implicit val decoder: Decoder[Disc] = deriveDecoder[Disc]

  def apply(radius: Double, p: Point): Disc = Disc(radius, p.x, p.y)
}

final case class Wedge(degrees: Double, radius: Double) extends Drawable {
  lazy val extent: Extent = Extent(2 * radius, 2 * radius)
  def draw(context: RenderContext): Unit = context.draw(this)
}
object Wedge {
  implicit val encoder: Encoder[Wedge] = deriveEncoder[Wedge]
  implicit val decoder: Decoder[Wedge] = deriveDecoder[Wedge]
}

final case class Translate(r: Drawable, x: Double = 0, y: Double = 0) extends Drawable {
  // TODO: is this correct with negative translations?
  lazy val extent: Extent = Extent(
    r.extent.width + x,
    r.extent.height + y
  )

  def draw(context: RenderContext): Unit = context.draw(this)
}
object Translate {
  implicit val encoder: Encoder[Translate] = deriveEncoder[Translate]
  implicit val decoder: Decoder[Translate] = deriveDecoder[Translate]
}

final case class Affine(r: Drawable, affine: AffineTransform) extends Drawable {
  lazy val extent: Extent = {
    val pts = Seq(affine(0, 0),
      affine(r.extent.width, 0),
      affine(r.extent.width, r.extent.height),
      affine(0, r.extent.height))
    val (xs, ys) = pts.unzip
    val width = xs.max - xs.min
    val height = ys.max - ys.min
    Extent(width, height)
  }

  def draw(context: RenderContext): Unit = context.draw(this)
}
object Affine {
  implicit val encoder: Encoder[Affine] = deriveEncoder[Affine]
  implicit val decoder: Decoder[Affine] = deriveDecoder[Affine]
}

final case class Scale(r: Drawable, x: Double = 1, y: Double = 1) extends Drawable {
  lazy val extent: Extent = Extent(r.extent.width * x, r.extent.height * y)
  def draw(context: RenderContext): Unit = context.draw(this)
}
object Scale {
  implicit val encoder: Encoder[Scale] = deriveEncoder[Scale]
  implicit val decoder: Decoder[Scale] = deriveDecoder[Scale]
}

// Our rotate semantics are, rotate about your centroid, and shift back to all positive coordinates
final case class Rotate(r: Drawable, degrees: Double) extends Drawable {

  lazy val radians: Double = math.toRadians(degrees)

  def originRotate(point: Point): Point = {
    Point(
      point.x * math.cos(radians) - point.y * math.sin(radians),
      point.y * math.cos(radians) + point.x * math.sin(radians)
    )
  }

  private lazy val rotatedCorners = Seq(
    Point(-0.5 * r.extent.width, -0.5 * r.extent.height),
    Point(0.5 * r.extent.width, -0.5 * r.extent.height),
    Point(-0.5 * r.extent.width, 0.5 * r.extent.height),
    Point(0.5 * r.extent.width, 0.5 * r.extent.height)
  ).map(originRotate)

  private[evilplot] lazy val minX: Double = rotatedCorners.minBy(_.x).x
  private[evilplot] lazy val maxX: Double = rotatedCorners.maxBy(_.x).x
  private[evilplot] lazy val minY: Double = rotatedCorners.minBy(_.y).y
  private[evilplot] lazy val maxY: Double = rotatedCorners.maxBy(_.y).y

  lazy val extent: Extent = Extent(maxX - minX, maxY - minY)

  def draw(context: RenderContext): Unit = context.draw(this)
}
object Rotate {
  implicit val encoder: Encoder[Rotate] = deriveEncoder[Rotate]
  implicit val decoder: Decoder[Rotate] = deriveDecoder[Rotate]
}

final case class Group(items: Seq[Drawable]) extends Drawable {
  lazy val extent: Extent = {
    if (items.isEmpty) {
      Extent(0, 0)
    } else {
      Extent(items.map(_.extent.width).max, items.map(_.extent.height).max)
    }
  }

  def draw(context: RenderContext): Unit = items.foreach(_.draw(context))
}
object Group {
  implicit val encoder: Encoder[Group] = deriveEncoder[Group]
  implicit val decoder: Decoder[Group] = deriveDecoder[Group]
}

// Change the size of the bounding box without changing the contents.
// This is used for padding below, for example.
final case class Resize(r: Drawable, extent: Extent) extends Drawable {
  def draw(context: RenderContext): Unit = r.draw(context)
}
object Resize {
  implicit val encoder: Encoder[Resize] = deriveEncoder[Resize]
  implicit val decoder: Decoder[Resize] = deriveDecoder[Resize]
}

final case class Style(r: Drawable, fill: Color) extends Drawable {
  lazy val extent: Extent = r.extent
  def draw(context: RenderContext): Unit = context.draw(this)
}
object Style {
  implicit val encoder: Encoder[Style] = deriveEncoder[Style]
  implicit val decoder: Decoder[Style] = deriveDecoder[Style]
}

/* for styling lines.
 * TODO: patterned (e.g. dashed, dotted) lines
 */
final case class StrokeStyle(r: Drawable, fill: Color) extends Drawable {
  lazy val extent: Extent = r.extent
  def draw(context: RenderContext): Unit = context.draw(this)
}
object StrokeStyle {
  implicit val encoder: Encoder[StrokeStyle] = deriveEncoder[StrokeStyle]
  implicit val decoder: Decoder[StrokeStyle] = deriveDecoder[StrokeStyle]
}

final case class StrokeWeight(r: Drawable, weight: Double) extends Drawable {
  lazy val extent: Extent = r.extent
  def draw(context: RenderContext): Unit = context.draw(this)
}
object StrokeWeight {
  implicit val encoder: Encoder[StrokeWeight] = deriveEncoder[StrokeWeight]
  implicit val decoder: Decoder[StrokeWeight] = deriveDecoder[StrokeWeight]
}

final case class Text(
  msg: String,
  size: Double = Text.defaultSize,
  extentOpt: Option[Extent] = None
) extends Drawable {
  require(size >= 0.5, s"Cannot use $size, canvas will not draw text initially sized < 0.5px even when scaling")

  lazy val extent: Extent = extentOpt.getOrElse(TextMetrics.measure(this))
  def draw(context: RenderContext): Unit = context.draw(this)
}
object Text {

  implicit val encoder: Encoder[Text] = Encoder.instance[Text] { text =>
    // Lock down the extents when encoding.
    deriveEncoder[Text].apply(text.copy(extentOpt = Some(text.extent)))
  }

  implicit val decoder: Decoder[Text] = deriveDecoder[Text]

  val defaultSize: Double = 10
}

object Drawable {
  require(implicitly[Configuration] == minifyProperties) // prevent auto removal.
  implicit val drawableEncoder: Encoder[Drawable] = deriveEncoder[Drawable]
  implicit val drawableDecoder: Decoder[Drawable] = deriveDecoder[Drawable]
}
