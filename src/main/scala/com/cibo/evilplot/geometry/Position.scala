package com.cibo.evilplot.geometry

import com.cibo.evilplot.{CanvasOp, Text}
import org.scalajs.dom._

case class Point(x: Double, y: Double)

case class Translate(x: Double = 0, y: Double = 0)(r: Renderable) extends Renderable {
  // TODO: is this correct with negative translations?
  val extent: Extent = Extent(
    r.extent.width + x,
    r.extent.height + y
  )

  def render(canvas: CanvasRenderingContext2D): Unit = CanvasOp(canvas){ c =>
    c.translate(x, y)
    r.render(c)
  }
}
object Translate {
  def apply(r: Renderable, bbox: Extent): Translate = Translate(bbox.width, bbox.height)(r)
}

case class Scale(x: Double = 1, y: Double = 1)(r: Renderable) extends Renderable {
  val extent = Extent( r.extent.width * y, r.extent.height * x )

  def render(canvas: CanvasRenderingContext2D): Unit = CanvasOp(canvas){ c =>
    c.scale(x, y)
    r.render(c)
  }
}

case class FlipY(r: Renderable) extends Renderable {
  val extent = r.extent

  def render(canvas: CanvasRenderingContext2D): Unit =
    Translate(y = r.extent.height){
      Scale(1, -1)(r)
    }.render(canvas)
}

case class FlipX(r: Renderable) extends Renderable {
  val extent = r.extent

  def render(canvas: CanvasRenderingContext2D): Unit =
    Translate(x = r.extent.width){
      Scale(-1, 1)(r)
    }.render(canvas)
}



// Our rotate semantics are, rotate about your centroid, and shift back to all positive coordinates
case class Rotate(degrees: Double)(r: Renderable) extends Renderable {

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

  private val rotatedCorners = Seq(
    Point(-0.5 * r.extent.width, -0.5 * r.extent.height),
    Point( 0.5 * r.extent.width, -0.5 * r.extent.height),
    Point(-0.5 * r.extent.width,  0.5 * r.extent.height),
    Point( 0.5 * r.extent.width,  0.5 * r.extent.height)
  ).map(_.originRotate(degrees))

  private val minX = rotatedCorners.map(_.x).min
  private val maxX = rotatedCorners.map(_.x).max
  private val minY = rotatedCorners.map(_.y).min
  private val maxY = rotatedCorners.map(_.y).max

  val extent = Extent(maxX - minX, maxY - minY)

  def render(canvas: CanvasRenderingContext2D): Unit =
    CanvasOp(canvas) { c =>
      c.translate(-1 * minX , -1 * minY)
      c.rotate(math.toRadians(degrees))
      c.translate(r.extent.width / -2, r.extent.height / -2)

      r.render(c)
    }
}

//TODO: A future way to eliminate this is:
// * replace "extents" and reaching into the object with a more sophisticated class
// * that class should support widestWidth, tallestHeight, and a rotate method that returns a new copy with same wW/tH
// * then extents can be arbitrary polygons instead of just Rect's
// end TODO

// Our rotate semantics are, rotate about your centroid, and shift back to all positive coordinates
// BUT CircularExtented things' rotated extents cannot be computed as a rotated rectangles, they are assumed invariant
case class UnsafeRotate(degrees: Double)(r: Renderable) extends Renderable {

  val extent = r.extent

  def render(canvas: CanvasRenderingContext2D): Unit =
    CanvasOp(canvas) { c =>
      c.translate(extent.width / 2, extent.height / 2)
      c.rotate(math.toRadians(degrees))
      c.translate(extent.width / -2, extent.height / -2)

      r.render(c)
    }
}

case class Pad(left: Double = 0, right: Double = 0, top: Double = 0, bottom: Double = 0)(item: Renderable) extends Renderable {
  val extent = Extent(
    item.extent.width + left + right,
    item.extent.height + top + bottom
  )

  def render(canvas: CanvasRenderingContext2D): Unit = Translate(x = left, y = top)(item).render(canvas)
}
object Pad {
  def apply(surround: Double)(item: Renderable): Pad = Pad(surround, surround, surround, surround)(item)
  def apply(x: Double, y: Double)(item: Renderable): Pad = Pad(x, x, y, y)(item)
}

case class Group(items: Renderable*) extends Renderable {
  val extent: Extent = Extent(
    items.map(_.extent.width).max,
    items.map(_.extent.height).max
  )

  def render(canvas: CanvasRenderingContext2D): Unit = items.foreach(_.render(canvas))
}

case class Above(top: Renderable, bottom: Renderable) extends Renderable {
  val extent: Extent = Extent(
    math.max(top.extent.width, bottom.extent.width),
    top.extent.height + bottom.extent.height
  )

  def render(canvas: CanvasRenderingContext2D): Unit = (
    top behind Translate(y = top.extent.height)(bottom)
    ).render(canvas)
}

case class Beside(head: Renderable, tail: Renderable) extends Renderable {
  def render(canvas: CanvasRenderingContext2D): Unit =
    (
      head behind Translate(x = head.extent.width)(tail)
      ).render(canvas)

  val extent: Extent = Extent(
    head.extent.width + tail.extent.width,
    math.max(head.extent.height, tail.extent.height)
  )
}


object Align {
  def bottomSeq(items: Seq[Renderable]) = bottom(items :_*)

  def bottom(items: Renderable*): Seq[Renderable] = {
    val groupHeight = items.maxBy(_.extent.height).extent.height

    items.map(r => Translate(y = groupHeight - r.extent.height)(r) )
  }

  def centerSeq(items: Seq[Renderable]) = center(items :_*)

  def center(items: Renderable*): Seq[Renderable] = {
    val groupWidth = items.maxBy(_.extent.width).extent.width

    items.map( r => Translate(x = (groupWidth - r.extent.width) / 2.0)(r) )
  }

  def right(items: Renderable*): Seq[Renderable] = {
    val groupWidth = items.maxBy(_.extent.width).extent.width

    items.map( r => Translate(x = groupWidth - r.extent.width)(r) )
  }

  def middle(items: Renderable*): Seq[Renderable] = {
    val groupHeight = items.maxBy(_.extent.height).extent.height

    items.map( r => Translate(y = (groupHeight - r.extent.height) / 2.0)(r) )
  }
}

case class Labeled(msg: String, r: Renderable, textSize: Double = Text.defaultSize) extends Renderable {

  private val composite = Align.center(r, Text(msg, textSize) padTop 5 ).reduce(Above)

  val extent: Extent = composite.extent
  def render(canvas: CanvasRenderingContext2D): Unit = composite.render(canvas)
}

case class Titled(msg: String, r: Renderable, textSize: Double = Text.defaultSize) extends Renderable {

  private val paddedTitle = Pad(bottom = textSize / 2.0)(Text(msg, textSize))
  private val composite = Align.center(paddedTitle, r).reduce(Above)

  val extent = composite.extent
  def render(canvas: CanvasRenderingContext2D): Unit = composite.render(canvas)
}

case class Fit(width: Double, height: Double)(item: Renderable) extends Renderable {
  val extent = Extent(width, height)

  def render(canvas: CanvasRenderingContext2D): Unit = {
    val oldExtent = item.extent

    val newAspectRatio = width / height
    val oldAspectRatio = oldExtent.width / oldExtent.height

    val widthIsLimiting = newAspectRatio < oldAspectRatio

    val (scale, padFun) = if(widthIsLimiting) {
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

    CanvasOp(canvas){c =>
      c.scale(scale, scale)
      padFun(item).render(c)
    }
  }
}
object Fit {
  def apply(extent: Extent)(item: Renderable): Fit = Fit(extent.width, extent.height)(item)
}



