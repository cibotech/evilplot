package com.cibo.evilplot.geometry

import com.cibo.evilplot.CanvasOp
import org.scalajs.dom._

/**
  * Extent defines an object's rectangular bounding box.
  * As discussed in <a href="http://ozark.hendrix.edu/~yorgey/pub/monoid-pearl.pdf">
  * "Monoids: Theme and Variations" by Yorgey</a>,
  * rectangular bounding boxes don't play well with rotation.
  * We'll eventually need something fancier like the convex hull.
  * @param width bounding box width
  * @param height bounding box height
  */
case class Extent(width: Double, height: Double) {
  def *(scale: Double): Extent = Extent(scale * width, scale * height)
}

/**
  * A DrawableLater defines an apply method that takes an extent and returns a Drawable. It enables plot element
  * construction without knowing size.
  */

trait DrawableLater {
  def apply(extent: Extent): Drawable
}

class DrawableLaterMaker(f: Extent => Drawable) extends DrawableLater {
  def apply(extent: Extent): Drawable = f(extent)
}

object EmptyDrawableLater extends DrawableLater {
  def apply(extent: Extent = Extent(0, 0)): Drawable = EmptyDrawable()
}

/**
  * All Drawable objects define a draw method that draws to a 2D canvas, and a bounding box (Extent).
  * The bounding box must not change.
  */
trait Drawable {
  val debug = true
  val extent: Extent
  def draw(canvas: CanvasRenderingContext2D): Unit
}

trait WrapDrawable extends Drawable {
  def drawable: Drawable
  override lazy val extent: Extent = drawable.extent
  override def draw(canvas: CanvasRenderingContext2D): Unit = drawable.draw(canvas)
}

case class EmptyDrawable(override val extent: Extent = Extent(0, 0)) extends Drawable {
  override def draw(canvas: CanvasRenderingContext2D): Unit = {}
}

case class Line(length: Double, strokeWidth: Double) extends Drawable {

  val extent = Extent(length, strokeWidth)

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
  val extent = Extent(xS.max - xS.min, yS.max - yS.min)

  def draw(canvas: CanvasRenderingContext2D): Unit =
    CanvasOp(canvas) { c =>
      canvas.beginPath()
      canvas.moveTo(points.head.x, points.head.y)
      canvas.lineWidth = strokeWidth
      points.tail.foreach(point => {
        canvas.lineTo(point.x, point.y)
      })
      canvas.stroke()
    }
}

case class Rect(width: Double, height: Double) extends Drawable {
  def draw(canvas: CanvasRenderingContext2D): Unit = canvas.fillRect(0, 0, width, height)
  val extent: Extent = Extent(width, height)
}

case class BorderRect(width: Double, height: Double) extends Drawable {
  def draw(canvas: CanvasRenderingContext2D): Unit = canvas.strokeRect(0, 0, width, height)
  val extent: Extent = Extent(width, height)
}

case class BorderFillRect(width: Double, height: Double) extends WrapDrawable {
  override def drawable: Drawable = BorderRect(width, height) behind Rect(width, height)
}

object Rect {
  def apply(side: Double): Rect = Rect(side, side)
  def apply(size: Extent): Rect = Rect(size.width, size.height)
}

/* TODO: The positioning of discs is accomplished by creating a big bounding box and
 * sticking the disc at its corner. It might be better to have a tight bounding box
 * and get the disc to the right position?
 */

case class Disc(radius: Double, x: Double = 0, y: Double = 0) extends Drawable {
  require(x >= 0 && y >=0, s"x {$x} and y {$y} must both be positive")
  val extent = Extent(x + radius * 2, y + radius * 2)

  def draw(canvas: CanvasRenderingContext2D): Unit =
    CanvasOp(canvas) { c =>
      c.beginPath()
      c.arc(x + radius, y + radius, radius, 0, 2 * Math.PI)
      c.closePath()
      c.fill()
//      c.strokeRect(0, 0, extent.width, extent.height)
    }
}

case class Wedge(angleDegrees: Double, radius: Double) extends Drawable {
  val extent = Extent(2 * radius, 2 * radius)

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
