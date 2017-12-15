/*
 * Copyright 2017 CiBO Technologies
 */

package com.cibo.evilplot.geometry

import com.cibo.evilplot.{CanvasOp, Text}
import org.scalajs.dom._




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
