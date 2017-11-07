/*
 * Copyright 2017 CiBO Technologies
 */

package com.cibo.evilplot

import com.cibo.evilplot.colors.Color
import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent}
import org.scalajs.dom
import org.scalajs.dom.CanvasRenderingContext2D

object Utils {
  val canvas: String = "CANVAS"
  val measureBuffer: String = "measureBuffer"

  def getCanvasFromElementId(id: String): CanvasRenderingContext2D = {
    // Muuuuuwahahahaha
    dom.window.document.getElementById(id)
      .asInstanceOf[dom.html.Canvas]
      .getContext("2d")
      .asInstanceOf[dom.CanvasRenderingContext2D]
  }
  def maybeDrawable[T](value: Option[T])(maker: T => Drawable): Drawable = {
    value match {
      case Some(t) => maker(t)
      case None => EmptyDrawable()
    }
  }

  def createNumericLabel(num: Double, numFrac: Int): String = {
    require(numFrac >= 0 && numFrac <= 20, "JavaScript does not support formatting fewer than 0" +
      s"or more than 20 decimal places, but you attempted to format with $numFrac")
    val fmtString = "%%.%df".format(numFrac)
    fmtString.format(num)
  }

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

// Run the passed-in rendering function, saving the canvas state before that, and restoring it afterwards.
object CanvasOp {
  def apply(canvas: CanvasRenderingContext2D)(f: CanvasRenderingContext2D => Unit): Unit = {
    canvas.save()
    f(canvas)
    canvas.restore()
  }
}
