package com.cibo.evilplot

import com.cibo.evilplot.colors.Color
import com.cibo.evilplot.geometry.{Extent, Renderable}
import org.scalajs.dom
import org.scalajs.dom.{html, _}

import scala.util.matching.Regex




case class Style(fill: Color)(r: Renderable) extends Renderable {
  val extent = r.extent
  def render(canvas: CanvasRenderingContext2D): Unit =
    CanvasOp(canvas){ c =>
      c.fillStyle = fill.repr
      r.render(c)
    }
}


case class Text(msgAny: Any, size: Double = Text.defaultSize) extends Renderable {
  require(size >= 0.5, s"Cannot use ${size}, canvas will not render text initially sized < 0.5px even when scaling")
  private val msg = msgAny.toString

  val extent: Extent = Text.measure(size)(msg)

  def render(canvas: CanvasRenderingContext2D): Unit = Text.withStyle(size){_.fillText(msg, 0, 0)}(canvas)
}
object Text {
  val defaultSize = 10

  // TODO: THIS IS A DIRTY HACK
  private val offscreenBuffer = dom.window.document.getElementById("measureBuffer").asInstanceOf[html.Canvas].getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

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

  private def measure(size: Double)(msg: String) = withStyle(size){ c =>
    Extent(c.measureText(msg).width, extractHeight)
  }(offscreenBuffer)
}

object CanvasOp {
  // loan it out
  def apply(canvas: CanvasRenderingContext2D)(f: CanvasRenderingContext2D => Unit) = {
    canvas.save()
    f(canvas)
    canvas.restore()
  }
}