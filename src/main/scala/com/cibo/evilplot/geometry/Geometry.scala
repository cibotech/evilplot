package com.cibo.evilplot.geometry

import com.cibo.evilplot.{CanvasOp}
import org.scalajs.dom._

case class Extent(width: Double, height: Double)
trait Renderable {
  // bounding boxen must be of stable size
  val extent: Extent
  def render(canvas: CanvasRenderingContext2D): Unit
}

case class Line(length: Double, strokeWidth: Double) extends Renderable {

  val extent = Extent(length, strokeWidth)

  def render(canvas: CanvasRenderingContext2D): Unit =
    CanvasOp(canvas) { c =>
      canvas.beginPath()
      canvas.lineWidth = strokeWidth
      canvas.moveTo(0, strokeWidth / 2.0)
      canvas.lineTo(length, strokeWidth / 2.0)
      canvas.closePath()
      canvas.stroke()
    }
}

case class Rect(width: Double, height: Double) extends Renderable {
  def render(canvas: CanvasRenderingContext2D): Unit = canvas.fillRect(0, 0, width, height)
  val extent: Extent = Extent(width, height)
}

object Rect {
  def apply(side: Double): Rect = Rect(side, side)
  def apply(size: Extent): Rect = Rect(size.width, size.height)
}

case class Disc(radius: Double, x: Double = 0, y: Double = 0) extends Renderable {
  require(x >= 0 && y >=0, s"x {$x} and y {$y} must both be positive")
  val extent = Extent(x + radius * 2, y + radius * 2)

  def render(canvas: CanvasRenderingContext2D): Unit =
    CanvasOp(canvas) { c =>
      c.beginPath()
      c.arc(x + radius, y + radius, radius, 0, 2 * Math.PI)
      c.closePath()
      c.fill()
    }
}

case class Wedge(angleDegrees: Double, radius: Double) extends Renderable {
  val extent = Extent(2 * radius, 2 * radius)

  def render(canvas: CanvasRenderingContext2D): Unit = {
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
