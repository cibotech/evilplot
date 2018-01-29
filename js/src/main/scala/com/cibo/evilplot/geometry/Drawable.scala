/*
 * Copyright 2017 CiBO Technologies
 */

package com.cibo.evilplot.geometry

import com.cibo.evilplot.colors.Color
import com.cibo.evilplot.Utils
import com.cibo.evilplot.numeric.{Point, Segment}
import org.scalajs.dom
import org.scalajs.dom.CanvasRenderingContext2D

/**
  * All Drawable objects define a draw method that draws to a 2D canvas, and a bounding box (Extent).
  * The bounding box must not change.
  */
sealed trait Drawable {
  def extent: Extent
  def draw(canvas: CanvasRenderingContext2D): Unit
}

case class EmptyDrawable(override val extent: Extent = Extent(0, 0)) extends Drawable {
  override def draw(canvas: CanvasRenderingContext2D): Unit = {}
}

case class Line(length: Double, strokeWidth: Double) extends Drawable {

  lazy val extent = Extent(length, strokeWidth)

  def draw(canvas: CanvasRenderingContext2D): Unit =
    CanvasOp(canvas) { c =>
      canvas.lineWidth = strokeWidth
      canvas.beginPath()
      canvas.moveTo(0, strokeWidth / 2.0)
      canvas.lineTo(length, strokeWidth / 2.0)
      canvas.closePath()
      canvas.stroke()
    }
}

case class Path(points: Seq[Point], strokeWidth: Double) extends Drawable {

  lazy val xS: Seq[Double] = points.map(_.x)
  lazy val yS: Seq[Double] = points.map(_.y)
  lazy val extent = Extent(xS.max - xS.min, yS.max - yS.min)
  private val correction = strokeWidth / 2.0

  def draw(canvas: CanvasRenderingContext2D): Unit =
    CanvasOp(canvas) { c =>
      canvas.beginPath()
      canvas.moveTo(points.head.x - correction, points.head.y + correction)
      canvas.lineWidth = strokeWidth
      points.tail.foreach(point => {
        canvas.lineTo(point.x - correction, point.y + correction)
      })
      canvas.stroke()
      // Uncomment this line in order to draw the bounding box for debugging
      //canvas.strokeRect(xS.min, yS.min, extent.width, extent.height)
    }
}

object Path {
  def apply(segment: Segment, strokeWidth: Double): Path = Path(Seq(segment.a, segment.b), strokeWidth)
}

case class Rect(width: Double, height: Double) extends Drawable {
  def draw(canvas: CanvasRenderingContext2D): Unit = canvas.fillRect(0, 0, width, height)
  lazy val extent: Extent = Extent(width, height)
}

case class BorderRect(width: Double, height: Double) extends Drawable {
  def draw(canvas: CanvasRenderingContext2D): Unit = canvas.strokeRect(0, 0, width, height)
  lazy val extent: Extent = Extent(width, height)
}

object Rect {
  def apply(side: Double): Rect = Rect(side, side)
  def apply(size: Extent): Rect = Rect(size.width, size.height)
  def borderFill(width: Double, height: Double): Drawable = BorderRect(width, height) inFrontOf Rect(width, height)
}

case class Disc(radius: Double, x: Double = 0, y: Double = 0) extends Drawable {
  require(x >= 0 && y >=0, s"x {$x} and y {$y} must both be positive")
  lazy val extent = Extent(x + radius, y + radius)

  def draw(canvas: CanvasRenderingContext2D): Unit =
    CanvasOp(canvas) { c =>
      c.beginPath()
      c.arc(x, y, radius, 0, 2 * Math.PI)
      c.closePath()
      c.fill()
    }
}

object Disc {
  def apply(radius: Double, p: Point): Disc = p match { case Point(x, y) => Disc(radius, x, y) }
}

case class Wedge(angleDegrees: Double, radius: Double) extends Drawable {
  lazy val extent = Extent(2 * radius, 2 * radius)

  def draw(canvas: CanvasRenderingContext2D): Unit = {
    CanvasOp(canvas) { c =>
      c.translate(radius, radius)
      c.beginPath()
      c.moveTo(0, 0)
      c.arc(0, 0, radius, -Math.PI * angleDegrees / 360.0, Math.PI * angleDegrees / 360.0)
      c.closePath()
      c.fill()
    }
  }
}

case class Translate(x: Double = 0, y: Double = 0)(r: Drawable) extends Drawable {
  // TODO: is this correct with negative translations?
  lazy val extent: Extent = Extent(
    r.extent.width + x,
    r.extent.height + y
  )

  def draw(canvas: CanvasRenderingContext2D): Unit = CanvasOp(canvas) { c =>
    c.translate(x, y)
    r.draw(c)
  }
}

object Translate {
  def apply(r: Drawable, bbox: Extent): Translate = Translate(bbox.width, bbox.height)(r)
  def apply(r: Drawable)(translate: Double): Translate = Translate(x = translate, y = translate)(r)
}

case class Affine(affine: AffineTransform)(r: Drawable) extends Drawable {
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

  def draw(canvas: CanvasRenderingContext2D): Unit = {
    CanvasOp(canvas){c =>
      c.transform(affine.scaleX, affine.shearX, affine.shearY, affine.scaleY, affine.shiftX, affine.shiftY)
      r.draw(c)
    }
  }
}

case class Scale(x: Double = 1, y: Double = 1)(r: Drawable) extends Drawable {
  lazy val extent: Extent = Extent(r.extent.width * x, r.extent.height * y)

  def draw(canvas: CanvasRenderingContext2D): Unit = CanvasOp(canvas) { c =>
    c.scale(x, y)
    r.draw(c)
  }
}

case class FlipY(height: Double)(r: Drawable) extends Drawable {
  lazy val extent: Extent = r.extent.copy(height = height)

  def draw(canvas: CanvasRenderingContext2D): Unit =
    Translate(y = extent.height) {
      Scale(1, -1)(r)
    }.draw(canvas)
}

object FlipY {
  def apply(r: Drawable): FlipY = FlipY(r.extent.height)(r)
}

case class FlipX(width: Double)(r: Drawable) extends Drawable {
  lazy val extent: Extent = r.extent.copy(width = width)

  def draw(canvas: CanvasRenderingContext2D): Unit =
    Translate(x = extent.width) {
      Scale(-1, 1)(r)
    }.draw(canvas)
}

object FlipX {
  def apply(r: Drawable): FlipX = FlipX(r.extent.width)(r)
}

// Our rotate semantics are, rotate about your centroid, and shift back to all positive coordinates
case class Rotate(degrees: Double)(r: Drawable) extends Drawable {

  // TODO: not bringing in a matrix library for just this one thing ... yet
  private case class Point(x: Double, y: Double) {
    def originRotate(thetaDegrees: Double): Point = {
      val thetaRad = math.toRadians(thetaDegrees)
      Point(
        x * math.cos(thetaRad) - y * math.sin(thetaRad),
        y * math.cos(thetaRad) + x * math.sin(thetaRad)
      )
    }
  }

  private lazy val rotatedCorners = Seq(
    Point(-0.5 * r.extent.width, -0.5 * r.extent.height),
    Point(0.5 * r.extent.width, -0.5 * r.extent.height),
    Point(-0.5 * r.extent.width, 0.5 * r.extent.height),
    Point(0.5 * r.extent.width, 0.5 * r.extent.height)
  ).map(_.originRotate(degrees))

  private lazy val minX = rotatedCorners.map(_.x).min
  private lazy val maxX = rotatedCorners.map(_.x).max
  private lazy val minY = rotatedCorners.map(_.y).min
  private lazy val maxY = rotatedCorners.map(_.y).max

  lazy val extent = Extent(maxX - minX, maxY - minY)

  def draw(canvas: CanvasRenderingContext2D): Unit =
    CanvasOp(canvas) { c =>
      c.translate(-1 * minX, -1 * minY)
      c.rotate(math.toRadians(degrees))
      c.translate(r.extent.width / -2, r.extent.height / -2)

      r.draw(c)
    }
}

//TODO: A future way to eliminate this is:
// * replace "extents" and reaching into the object with a more sophisticated class
// * that class should support widestWidth, tallestHeight, and a rotate method that returns a new copy with same wW/tH
// * then extents can be arbitrary polygons instead of just Rect's
// end TODO

// Our rotate semantics are, rotate about your centroid, and shift back to all positive coordinates
// BUT CircularExtented things' rotated extents cannot be computed as a rotated rectangles, they are assumed invariant
case class UnsafeRotate(degrees: Double)(r: Drawable) extends Drawable {

  lazy val extent: Extent = r.extent

  def draw(canvas: CanvasRenderingContext2D): Unit =
    CanvasOp(canvas) { c =>
      c.translate(extent.width / 2, extent.height / 2)
      c.rotate(math.toRadians(degrees))
      c.translate(extent.width / -2, extent.height / -2)

      r.draw(c)
    }
}

case class Pad(left: Double = 0, right: Double = 0, top: Double = 0, bottom: Double = 0)(item: Drawable)
  extends Drawable {
  lazy val extent = Extent(
    item.extent.width + left + right,
    item.extent.height + top + bottom
  )

  def draw(canvas: CanvasRenderingContext2D): Unit = {
    Translate(x = left, y = top)(item).draw(canvas)
  }
}

object Pad {
  def apply(surround: Double)(item: Drawable): Pad = Pad(surround, surround, surround, surround)(item)
  def apply(x: Double, y: Double)(item: Drawable): Pad = Pad(x, x, y, y)(item)
}

case class Debug(r: Drawable) extends Drawable {
  lazy val extent: Extent = r.extent

  def draw(canvas: CanvasRenderingContext2D): Unit = {
    CanvasOp(canvas) { c =>
      val hexDigits = "0123456789ABCDEF"
      c.strokeStyle = (0 until 3).map(_ => math.random * 255.0)
        .map(v => s"${hexDigits(v.toInt >> 4)}${hexDigits(v.toInt & 15)}")
        .mkString("#", "", "")

      c.strokeRect(0, 0, extent.width, extent.height)
    }
    r.draw(canvas)
  }
}

case class Group(items: Drawable*) extends Drawable {
  lazy val extent: Extent = {
    if (items.toSeq.isEmpty) {
      Extent(0, 0)
    } else {
      Extent(items.map(_.extent.width).max, items.map(_.extent.height).max)
    }
  }

  def draw(canvas: CanvasRenderingContext2D): Unit = if (items.toSeq.isEmpty) () else items.foreach(_.draw(canvas))
}

case class Above(top: Drawable, bottom: Drawable) extends Drawable {
  lazy val extent: Extent = Extent(
    math.max(top.extent.width, bottom.extent.width),
    top.extent.height + bottom.extent.height
  )

  def draw(canvas: CanvasRenderingContext2D): Unit = (
    top behind Translate(y = top.extent.height)(bottom)
    ).draw(canvas)
}

case class Beside(head: Drawable, tail: Drawable) extends Drawable {
  def draw(canvas: CanvasRenderingContext2D): Unit =
    (
      head behind Translate(x = head.extent.width)(tail)
      ).draw(canvas)

  lazy val extent: Extent = Extent(
    head.extent.width + tail.extent.width,
    math.max(head.extent.height, tail.extent.height)
  )
}


object Align {
  def bottomSeq(items: Seq[Drawable]): Seq[Drawable] = bottom(items: _*)

  def bottom(items: Drawable*): Seq[Drawable] = {
    lazy val groupHeight = items.maxBy(_.extent.height).extent.height

    items.map(r => Translate(y = groupHeight - r.extent.height)(r))
  }

  def centerSeq(items: Seq[Drawable]): Seq[Drawable] = center(items: _*)

  def center(items: Drawable*): Seq[Drawable] = {
    lazy val groupWidth = items.maxBy(_.extent.width).extent.width

    items.map(r => Translate(x = (groupWidth - r.extent.width) / 2.0)(r))
  }

  def right(items: Drawable*): Seq[Drawable] = {
    lazy val groupWidth = items.maxBy(_.extent.width).extent.width

    items.map(r => Translate(x = groupWidth - r.extent.width)(r))
  }

  def rightSeq(items: Seq[Drawable]): Seq[Drawable] = right(items: _*)

  def middleSeq(items: Seq[Drawable]): Seq[Drawable] = middle(items: _*)

  def middle(items: Drawable*): Seq[Drawable] = {
    lazy val groupHeight = items.maxBy(_.extent.height).extent.height

    items.map(r => Translate(y = (groupHeight - r.extent.height) / 2.0)(r))
  }
}

case class Labeled(msg: String, r: Drawable, textSize: Double = Text.defaultSize) extends Drawable {

  private val composite = Align.center(r, Text(msg, textSize) padTop 5).group

  lazy val extent: Extent = composite.extent
  def draw(canvas: CanvasRenderingContext2D): Unit = composite.draw(canvas)
}

case class Titled(msg: String, r: Drawable, textSize: Double = Text.defaultSize) extends Drawable {

  private val paddedTitle = Pad(bottom = textSize / 2.0)(Text(msg, textSize))
  private val composite = Align.center(paddedTitle, r).reduce(Above)

  lazy val extent: Extent = composite.extent
  def draw(canvas: CanvasRenderingContext2D): Unit = composite.draw(canvas)
}

case class Fit(width: Double, height: Double)(item: Drawable) extends Drawable {
  lazy val extent = Extent(width, height)

  def draw(canvas: CanvasRenderingContext2D): Unit = {
    val oldExtent = item.extent

    val newAspectRatio = width / height
    val oldAspectRatio = oldExtent.width / oldExtent.height

    val widthIsLimiting = newAspectRatio < oldAspectRatio

    val (scale, padFun) = if (widthIsLimiting) {
      val scale = width / oldExtent.width
      (
        scale,
        Pad(top = ((height - oldExtent.height * scale) / 2) / scale) _
      )
    } else { // height is limiting
      val scale = height / oldExtent.height
      (
        scale,
        Pad(left = ((width - oldExtent.width * scale) / 2) / scale) _
      )
    }

    CanvasOp(canvas) {c =>
      c.scale(scale, scale)
      padFun(item).draw(c)
    }
  }
}

object Fit {
  def apply(extent: Extent)(item: Drawable): Fit = Fit(extent.width, extent.height)(item)
}

case class Style(fill: Color)(r: Drawable) extends Drawable {
  val extent: Extent = r.extent
  def draw(canvas: CanvasRenderingContext2D): Unit =
    CanvasOp(canvas) { c =>
      c.fillStyle = fill.repr
      r.draw(c)
    }
}

/* for styling lines.
 * TODO: patterned (e.g. dashed, dotted) lines
 */
case class StrokeStyle(fill: Color)(r: Drawable) extends Drawable {
  val extent: Extent = r.extent
  def draw(canvas: dom.CanvasRenderingContext2D): Unit =
    CanvasOp(canvas) { c =>
      c.strokeStyle = fill.repr
      r.draw(c)
    }
}

case class StrokeWeight(weight: Double)(r: Drawable) extends Drawable {
  val extent: Extent = r.extent
  def draw(canvas: dom.CanvasRenderingContext2D): Unit = CanvasOp(canvas) { c =>
    c.lineWidth = weight
    r.draw(c)
  }
}

case class Text(msgAny: Any, size: Double = Text.defaultSize) extends Drawable {
  require(size >= 0.5, s"Cannot use $size, canvas will not draw text initially sized < 0.5px even when scaling")
  private val msg = msgAny.toString

  lazy val extent: Extent = Text.measure(size)(msg)

  def draw(canvas: dom.CanvasRenderingContext2D): Unit = Text.withStyle(size) {_.fillText(msg, 0, 0)}(canvas)
}

/* TODO: Currently, we draw some text to a canvas element, measure the text, and calculate an extent based on our
 * measurement. This is a hack. To get around it, all extents are lazily evaluated, and it is only when a draw
 * method is called that any of these canvas calls will be made. So, you can play with Drawables on the JVM as long
 * as you never call `draw`
 *
 * This works for now, but we're asking for trouble if we continue to operate this way.
 *  We could make use of the maxWidth arg to CanvasRenderingContext2D to at least get an upper bound on text size
 *  without an explicit measurement.
 */
object Text {
  val defaultSize = 10

  private lazy val offscreenBuffer: dom.CanvasRenderingContext2D = {
    Utils.getCanvasFromElementId("measureBuffer")
  }
  private lazy val replaceSize = """\d+px""".r
  // TODO: Text this regex esp on 1px 1.0px 1.px .1px, what is valid in CSS?
  private lazy val fontSize = """[^\d]*([\d(?:\.\d*)]+)px.*""".r
  private def extractHeight = {
    val fontSize(size) = offscreenBuffer.font
    size.toDouble
  }

  private def swapFont(canvas: dom.CanvasRenderingContext2D, size: Double) = {
    Text.replaceSize.replaceFirstIn(canvas.font, size.toString + "px")
  }

  private def withStyle[T](size: Double)(f: dom.CanvasRenderingContext2D => T): dom.CanvasRenderingContext2D => T = {
    c =>
      c.textBaseline = "top"
      c.font = swapFont(c, size)
      f(c)
  }

  private def measure(size: Double)(msg: String) = withStyle(size) { c =>
    Extent(c.measureText(msg).width, extractHeight)
  }(offscreenBuffer)
}
