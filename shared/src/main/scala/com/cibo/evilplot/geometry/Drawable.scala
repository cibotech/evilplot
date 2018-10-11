/*
 * Copyright (c) 2018, CiBO Technologies, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.cibo.evilplot.geometry

import com.cibo.evilplot.JSONUtils.minifyProperties
import com.cibo.evilplot.colors.Color
import com.cibo.evilplot.numeric.{Point, Point2d}
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import io.circe._
// scalastyle:off

/**
  * All Drawable objects define a draw method that draws to a 2D canvas, and a bounding box (Extent).
  * The bounding box must not change.
  */
sealed trait Drawable {
  def extent: Extent
  def draw(context: RenderContext): Unit

  private[evilplot] def isEmpty: Boolean = false
}

// On the fragility of adding primitives:
// Because of the way the JSON de/serialization works right now, no two field names
// can start with the same character in any class extending Drawable.
// Also you should register a shortened constructor name in JSONUtils#shortenedName

sealed trait InteractionEvent {
  val e: () => Unit
}

// Interaction events are non-portable
object InteractionEvent {
  implicit val encodeInteractionEvent: Encoder[InteractionEvent] = new Encoder[InteractionEvent] {
    final def apply(a: InteractionEvent): Json = Json.obj()
  }

  implicit val decodeInteractionEvent: Decoder[InteractionEvent] = new Decoder[InteractionEvent] {
    final def apply(c: HCursor): Decoder.Result[InteractionEvent] = Right(EmptyEvent())
  }
}

case class EmptyEvent() extends InteractionEvent{
  override val e: () => Unit = () => ()
}
case class OnClick(e: () => Unit) extends InteractionEvent
case class OnHover(e: () => Unit) extends InteractionEvent

/** Apply a fill color to a fillable Drawable. */
final case class Interaction(r: Drawable, interactionEvent: InteractionEvent*) extends Drawable {
  lazy val extent: Extent = r.extent
  def draw(context: RenderContext): Unit = context.draw(this)
}

object Interaction {
  implicit val encoder: Encoder[Interaction] = deriveEncoder[Interaction]
  implicit val decoder: Decoder[Interaction] = deriveDecoder[Interaction]
}

/** A drawable that displays nothing when drawn. */
final case class EmptyDrawable() extends Drawable {
  val extent: Extent = Extent(0, 0)
  def draw(context: RenderContext): Unit = ()

  override private[evilplot] def isEmpty: Boolean = true
}
object EmptyDrawable {
  implicit val encoder: Encoder[EmptyDrawable] = deriveEncoder[EmptyDrawable]
  implicit val decoder: Decoder[EmptyDrawable] = deriveDecoder[EmptyDrawable]
}

/** A horizontal line.
  * @param length the length of the line
  * @param strokeWidth the thickness of the line
  */
final case class Line(length: Double, strokeWidth: Double) extends Drawable {
  lazy val extent = Extent(length, strokeWidth)
  def draw(context: RenderContext): Unit = context.draw(this)
}
object Line {
  implicit val encoder: Encoder[Line] = deriveEncoder[Line]
  implicit val decoder: Decoder[Line] = deriveDecoder[Line]
}

/** A path with a strokable outline.
  * @param points The points in the path. The path will be drawn in the same
  *               order as the provided sequence.
  * @param strokeWidth The thickness of the line. */
final case class Path(points: Seq[Point], strokeWidth: Double) extends Drawable {
  private lazy val xS: Seq[Double] = points.map(_.x)
  private lazy val yS: Seq[Double] = points.map(_.y)
  lazy val extent: Extent =
    if (points.nonEmpty) Extent(xS.max, yS.max) else Extent(0, 0)
  def draw(context: RenderContext): Unit = if (points.nonEmpty) context.draw(this) else ()
}
object Path {

  def apply(points: Seq[Point2d], strokeWidth: Double): Drawable = {
    val minX = points.map(_.x).min
    val minY = points.map(_.y).min
    Translate(new Path(points.map { x =>
      Point(x.x - minX, x.y - minY)
    }, strokeWidth), minX, minY)
  }
  implicit val encoder: Encoder[Path] = Encoder.forProduct2("p", "s") { x =>
    (x.points, x.strokeWidth)
  }
  implicit val decoder: Decoder[Path] = Decoder.forProduct2("p", "s")(
    (points: Seq[Point], strokeWidth: Double) => new Path(points, strokeWidth)
  )
}

/** A filled polygon.
  * @param boundary the points on the boundary of the polygon.
  */
final case class Polygon(boundary: Seq[Point]) extends Drawable {
  private lazy val xS: Seq[Double] = boundary.map(_.x)
  private lazy val yS: Seq[Double] = boundary.map(_.y)
  lazy val extent: Extent =
    if (boundary.nonEmpty) Extent(xS.max, yS.max) else Extent(0, 0)
  def draw(context: RenderContext): Unit = if (boundary.nonEmpty) context.draw(this) else ()
}
object Polygon {
  implicit val encoder: Encoder[Polygon] = Encoder.forProduct1("b") { x =>
    x.boundary
  }
  implicit val decoder: Decoder[Polygon] = Decoder.forProduct1("b")(
    (boundary: Seq[Point]) => new Polygon(boundary)
  )

  def clipped(boundary: Seq[Point], extent: Extent): Drawable = {
    Polygon(Clipping.clipPolygon(boundary, extent))
  }

  def apply(boundary: Seq[Point2d]): Drawable = {
    val minX = boundary.map(_.x).min
    val minY = boundary.map(_.y).min
    Translate(new Polygon(boundary.map { x =>
      Point(x.x - minX, x.y - minY)
    }), minX, minY)
  }
}

/** A filled rectangle.
  * @param width the width of the rectangle
  * @param height the height of the rectangle
  */
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

/** A rectangle whose outline may be colored.
  * @param width the width of the rectangle
  * @param height the height of the rectangle
  */
final case class BorderRect(width: Double, height: Double) extends Drawable {
  lazy val extent: Extent = Extent(width, height)
  def draw(context: RenderContext): Unit = context.draw(this)
}
object BorderRect {
  implicit val encoder: Encoder[BorderRect] = deriveEncoder[BorderRect]
  implicit val decoder: Decoder[BorderRect] = deriveDecoder[BorderRect]

  /** A rectangle that can be both filled and stroked. */
  def filled(width: Double, height: Double): Drawable = {
    Seq(Rect(width, height), BorderRect(width, height)).group
  }
}

/** A filled disc.
  * @note Like all `Drawable`s, `Disc`s are positioned from their upper
  * left corner. Call `Disc.centered` to create a `Disc` that can be positioned
  * from its vertex.
  */
final case class Disc(radius: Double) extends Drawable {
  lazy val extent = Extent(radius * 2, radius * 2)

  def draw(context: RenderContext): Unit = context.draw(this)
}
object Disc {

  /** Create a disc that can be positioned from its vertex. */
  def centered(radius: Double): Drawable = Disc(radius).translate(-radius, -radius)

  implicit val encoder: Encoder[Disc] = deriveEncoder[Disc]
  implicit val decoder: Decoder[Disc] = deriveDecoder[Disc]
}

/** A piece of a circle. */
final case class Wedge(degrees: Double, radius: Double) extends Drawable {
  lazy val extent: Extent = Extent(2 * radius, 2 * radius)
  def draw(context: RenderContext): Unit = context.draw(this)
}
object Wedge {
  implicit val encoder: Encoder[Wedge] = deriveEncoder[Wedge]
  implicit val decoder: Decoder[Wedge] = deriveDecoder[Wedge]
}

/** Translate the passed in Drawable. */
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

/** Apply an affine transformation to the passed in Drawable. */
final case class Affine(r: Drawable, affine: AffineTransform) extends Drawable {
  lazy val extent: Extent = {
    val pts = Seq(
      affine(0, 0),
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

/** Scale the passed in Drawable. */
final case class Scale(r: Drawable, x: Double = 1, y: Double = 1) extends Drawable {
  lazy val extent: Extent = Extent(r.extent.width * x, r.extent.height * y)
  def draw(context: RenderContext): Unit = context.draw(this)
}
object Scale {
  implicit val encoder: Encoder[Scale] = deriveEncoder[Scale]
  implicit val decoder: Decoder[Scale] = deriveDecoder[Scale]
}

// Our rotate semantics are, rotate about your centroid, and shift back to all positive coordinates
/** Rotate the passed in Drawable. */
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

/** Combined a sequence of Drawables into a single Drawable. */
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
/** Change the size of the bounding box of a Drawable without changing the
  * contents.
  */
final case class Resize(r: Drawable, extent: Extent) extends Drawable {
  def draw(context: RenderContext): Unit = r.draw(context)
}
object Resize {
  implicit val encoder: Encoder[Resize] = deriveEncoder[Resize]
  implicit val decoder: Decoder[Resize] = deriveDecoder[Resize]
}

/** Apply a fill color to a fillable Drawable. */
final case class Style(r: Drawable, fill: Color) extends Drawable {
  lazy val extent: Extent = r.extent
  def draw(context: RenderContext): Unit = context.draw(this)
}
object Style {
  implicit val encoder: Encoder[Style] = deriveEncoder[Style]
  implicit val decoder: Decoder[Style] = deriveDecoder[Style]
}

/** Apply a gradient fill to a fillable Drawable. */
final case class GradientFill(r: Drawable, fill: Gradient2d) extends Drawable {
  lazy val extent: Extent = r.extent
  def draw(context: RenderContext): Unit = context.draw(this)
}
object GradientFill {
  implicit val encoder: Encoder[GradientFill] = deriveEncoder[GradientFill]
  implicit val decoder: Decoder[GradientFill] = deriveDecoder[GradientFill]
}

/** Apply a border color to a strokable Drawable. */
final case class StrokeStyle(r: Drawable, fill: Color) extends Drawable {
  lazy val extent: Extent = r.extent
  def draw(context: RenderContext): Unit = context.draw(this)
}
object StrokeStyle {
  implicit val encoder: Encoder[StrokeStyle] = deriveEncoder[StrokeStyle]
  implicit val decoder: Decoder[StrokeStyle] = deriveDecoder[StrokeStyle]
}

/** Adjust the thickness of the border on a strokeable Drawable. */
final case class StrokeWeight(r: Drawable, weight: Double) extends Drawable {
  lazy val extent: Extent = r.extent
  def draw(context: RenderContext): Unit = context.draw(this)
}
object StrokeWeight {
  implicit val encoder: Encoder[StrokeWeight] = deriveEncoder[StrokeWeight]
  implicit val decoder: Decoder[StrokeWeight] = deriveDecoder[StrokeWeight]
}

/** Apply a line dash to a Drawable.
  * @param r The drawable to apply the style to.
  * @param style The LineStyle to apply.
  */
final case class LineDash(r: Drawable, style: LineStyle) extends Drawable {
  lazy val extent: Extent = r.extent
  def draw(context: RenderContext): Unit = context.draw(this)
}
object LineDash {
  implicit val encoder: Encoder[LineDash] = deriveEncoder[LineDash]
  implicit val decoder: Decoder[LineDash] = deriveDecoder[LineDash]
}

/** Some text.
  * @param msg the string to render.
  * @param size the font size of the text.
  */
final case class Text(
  msg: String,
  size: Double = Text.defaultSize,
  fontFace: String = Text.defaultFontFace,
  extentOpt: Option[Extent] = None
) extends Drawable {
  require(
    size >= 0.5,
    s"Cannot use $size, canvas will not draw text initially sized < 0.5px even when scaling")

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
  val defaultFontFace: String = "sans-serif"
}

object Drawable {
  require(implicitly[Configuration] == minifyProperties) // prevent auto removal.
  implicit val drawableEncoder: Encoder[Drawable] = deriveEncoder[Drawable]
  implicit val drawableDecoder: Decoder[Drawable] = deriveDecoder[Drawable]
}

// scalastyle:on
