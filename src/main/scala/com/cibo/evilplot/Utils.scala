package com.cibo.evilplot

import com.cibo.evilplot.colors.Color
import com.cibo.evilplot.geometry.{AffineTransform, Drawable, DrawableLater, EmptyDrawable, EmptyDrawableLater, Extent}
import org.scalajs.dom
import org.scalajs.dom.{html, _}

object Utils {
  val canvas: String = "CANVAS"
  val measureBuffer: String = "measureBuffer"

  def getCanvasFromElementId(id: String): dom.CanvasRenderingContext2D = {
    // Muuuuuwahahahaha
    dom.window.document.getElementById(id)
      .asInstanceOf[html.Canvas]
      .getContext("2d")
      .asInstanceOf[dom.CanvasRenderingContext2D]
  }

  def maybeDrawable[T](value: Option[T], maker: T => Drawable, default: Drawable = EmptyDrawable()): Drawable = {
    value match {
      case Some(t) => maker(t)
      case None => default
    }
  }

  def maybeDrawableLater[T](value: Option[T], maker: T => DrawableLater): DrawableLater = {
    value match {
      case Some(t) => maker(t)
      case None => EmptyDrawableLater
    }
  }

  def createNumericLabel(num: Double, numFrac: Int): String = {
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
  def draw(canvas: CanvasRenderingContext2D): Unit =
    CanvasOp(canvas) { c =>
      c.strokeStyle = fill.repr
      r.draw(c)
    }
}


case class Text(msgAny: Any, size: Double = Text.defaultSize) extends Drawable {
  require(size >= 0.5, s"Cannot use $size, canvas will not draw text initially sized < 0.5px even when scaling")
  private val msg = msgAny.toString

  val extent: Extent = Text.measure(size)(msg)

  def draw(canvas: CanvasRenderingContext2D): Unit = Text.withStyle(size) {_.fillText(msg, 0, 0)}(canvas)
}
object Text {
  val defaultSize = 10

  // TODO: THIS IS A DIRTY HACK
  private val offscreenBuffer: CanvasRenderingContext2D = Utils.getCanvasFromElementId("measureBuffer")
  private val replaceSize = """\d+px""".r
  // TODO: Text this regex esp on 1px 1.0px 1.px .1px, what is valid in CSS?
  private val fontSize = """[^\d]*([\d(?:\.\d*)]+)px.*""".r
  private def extractHeight = {
    val fontSize(size) = offscreenBuffer.font
    size.toDouble
  }

  private def swapFont(canvas: CanvasRenderingContext2D, size: Double) = {
    Text.replaceSize.replaceFirstIn(canvas.font, size.toString + "px")
  }

  private def withStyle[T](size: Double)(f: CanvasRenderingContext2D => T): CanvasRenderingContext2D => T = {
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
