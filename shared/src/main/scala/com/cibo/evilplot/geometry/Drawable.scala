/*
 * Copyright 2017 CiBO Technologies
 */

package com.cibo.evilplot.geometry

import com.cibo.evilplot.colors.Color
import com.cibo.evilplot.numeric.{Point, Segment}
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._

/**
  * All Drawable objects define a draw method that draws to a 2D canvas, and a bounding box (Extent).
  * The bounding box must not change.
  */
sealed trait Drawable {
  def extent: Extent
  def draw(context: RenderContext): Unit
}

final case class EmptyDrawable(extent: Extent = Extent(0, 0)) extends Drawable {
  def draw(context: RenderContext): Unit = ()
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
  lazy val extent = Extent(xS.max - xS.min, yS.max - yS.min)
  def draw(context: RenderContext): Unit = context.draw(this)
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
    Group(Seq(Rect(width, height), BorderRect(width, height)))
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
  lazy val extent = Extent(2 * radius, 2 * radius)
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

  def apply(r: Drawable, bbox: Extent): Translate = Translate(r, bbox.width, bbox.height)
  def apply(r: Drawable)(translate: Double): Translate = Translate(r, x = translate, y = translate)
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

  lazy val extent = Extent(maxX - minX, maxY - minY)

  def draw(context: RenderContext): Unit = context.draw(this)
}
object Rotate {
  implicit val encoder: Encoder[Rotate] = deriveEncoder[Rotate]
  implicit val decoder: Decoder[Rotate] = deriveDecoder[Rotate]
}

//TODO: A future way to eliminate this is:
// * replace "extents" and reaching into the object with a more sophisticated class
// * that class should support widestWidth, tallestHeight, and a rotate method that returns a new copy with same wW/tH
// * then extents can be arbitrary polygons instead of just Rect's
// end TODO

// Our rotate semantics are, rotate about your centroid, and shift back to all positive coordinates
// BUT CircularExtented things' rotated extents cannot be computed as a rotated rectangles, they are assumed invariant
final case class UnsafeRotate(r: Drawable, degrees: Double) extends Drawable {
  lazy val extent: Extent = r.extent
  def draw(context: RenderContext): Unit = context.draw(this)
}
object UnsafeRotate {
  implicit val encoder: Encoder[UnsafeRotate] = deriveEncoder[UnsafeRotate]
  implicit val decoder: Decoder[UnsafeRotate] = deriveDecoder[UnsafeRotate]
}

final case class Pad(
  item: Drawable,
  left: Double = 0,
  right: Double = 0,
  top: Double = 0,
  bottom: Double = 0
) extends Drawable {
  lazy val extent = Extent(
    item.extent.width + left + right,
    item.extent.height + top + bottom
  )

  def draw(context: RenderContext): Unit = Translate(item, x = left, y = top).draw(context)
}

object Pad {
  implicit val encoder: Encoder[Pad] = deriveEncoder[Pad]
  implicit val decoder: Decoder[Pad] = deriveDecoder[Pad]

  def apply(surround: Double)(item: Drawable): Pad = Pad(item, surround, surround, surround, surround)
  def apply(x: Double, y: Double)(item: Drawable): Pad = Pad(item, x, x, y, y)
}

final case class Debug(r: Drawable) extends Drawable {
  lazy val extent: Extent = r.extent
  def draw(context: RenderContext): Unit = context.draw(this)
}
object Debug {
  implicit val encoder: Encoder[Debug] = deriveEncoder[Debug]
  implicit val decoder: Decoder[Debug] = deriveDecoder[Debug]
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

final case class FlipY(r: Drawable, height: Double) extends Drawable {
  lazy val extent: Extent = r.extent.copy(height = height)
  def draw(context: RenderContext): Unit = Translate(Scale(r, 1, -1), y = height).draw(context)
}

final case class FlipX(r: Drawable, width: Double) extends Drawable {
  lazy val extent: Extent = r.extent.copy(width = width)
  def draw(context: RenderContext): Unit = Translate(Scale(r, -1, 1), x = width).draw(context)
}

//TODO: this doesn't need to be a drawable.
final case class Above(top: Drawable, bottom: Drawable) extends Drawable {
  lazy val extent: Extent = Extent(
    math.max(top.extent.width, bottom.extent.width),
    top.extent.height + bottom.extent.height
  )

  def draw(context: RenderContext): Unit = {
    top.draw(context)
    Translate(bottom, y = top.extent.height).draw(context)
  }
}
object Above {
  implicit val encoder: Encoder[Above] = deriveEncoder[Above]
  implicit val decoder: Decoder[Above] = deriveDecoder[Above]
}

//TODO: this doesn't need to be a drawable.
final case class Beside(head: Drawable, tail: Drawable) extends Drawable {
  lazy val extent: Extent = Extent(
    head.extent.width + tail.extent.width,
    math.max(head.extent.height, tail.extent.height)
  )

  def draw(context: RenderContext): Unit = {
    head.draw(context)
    Translate(tail, x = head.extent.width).draw(context)
  }
}
object Beside {
  implicit val encoder: Encoder[Beside] = deriveEncoder[Beside]
  implicit val decoder: Decoder[Beside] = deriveDecoder[Beside]
}

//TODO: pull this out
object Align {
  def bottomSeq(items: Seq[Drawable]): Seq[Drawable] = bottom(items: _*)

  def bottom(items: Drawable*): Seq[Drawable] = {
    lazy val groupHeight = items.maxBy(_.extent.height).extent.height

    items.map(r => Translate(r, y = groupHeight - r.extent.height))
  }

  def centerSeq(items: Seq[Drawable]): Seq[Drawable] = center(items: _*)

  def center(items: Drawable*): Seq[Drawable] = {
    lazy val groupWidth = items.maxBy(_.extent.width).extent.width

    items.map(r => Translate(r, x = (groupWidth - r.extent.width) / 2.0))
  }

  def right(items: Drawable*): Seq[Drawable] = {
    lazy val groupWidth = items.maxBy(_.extent.width).extent.width

    items.map(r => Translate(r, x = groupWidth - r.extent.width))
  }

  def rightSeq(items: Seq[Drawable]): Seq[Drawable] = right(items: _*)

  def middleSeq(items: Seq[Drawable]): Seq[Drawable] = middle(items: _*)

  def middle(items: Drawable*): Seq[Drawable] = {
    lazy val groupHeight = items.maxBy(_.extent.height).extent.height

    items.map(r => Translate(r, y = (groupHeight - r.extent.height) / 2.0))
  }
}

//TODO: pull this out
final case class Fit(item: Drawable, width: Double, height: Double) extends Drawable {
  lazy val extent = Extent(width, height)

  def draw(context: RenderContext): Unit = {
    val oldExtent = item.extent

    val newAspectRatio = width / height
    val oldAspectRatio = oldExtent.width / oldExtent.height

    val widthIsLimiting = newAspectRatio < oldAspectRatio

    val (scale, padFun) = if (widthIsLimiting) {
      val scale = width / oldExtent.width
      (
        scale,
        (d: Drawable) => Pad(d, top = ((height - oldExtent.height * scale) / 2) / scale)
      )
    } else { // height is limiting
      val scale = height / oldExtent.height
      (
        scale,
        (d:Drawable) => Pad(d, left = ((width - oldExtent.width * scale) / 2) / scale)
      )
    }

    Scale(padFun(item), scale, scale).draw(context)
  }
}

object Fit {
  implicit val encoder: Encoder[Fit] = deriveEncoder[Fit]
  implicit val decoder: Decoder[Fit] = deriveDecoder[Fit]
  def apply(extent: Extent)(item: Drawable): Fit = Fit(item, extent.width, extent.height)
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

final case class Text(msg: String, size: Double = Text.defaultSize) extends Drawable {
  require(size >= 0.5, s"Cannot use $size, canvas will not draw text initially sized < 0.5px even when scaling")

  lazy val extent: Extent = TextMetrics.measure(this)
  def draw(context: RenderContext): Unit = context.draw(this)
}
object Text {
  implicit val encoder: Encoder[Text] = deriveEncoder[Text]
  implicit val decoder: Decoder[Text] = deriveDecoder[Text]

  val defaultSize: Double = 10
}

object Drawable {
  implicit val drawableEncoder: Encoder[Drawable] = io.circe.generic.semiauto.deriveEncoder[Drawable]
  implicit val drawableDecoder: Decoder[Drawable] = io.circe.generic.semiauto.deriveDecoder[Drawable]
}
