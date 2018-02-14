package com.cibo.evilplot.geometry

import com.cibo.evilplot.Utils
import org.scalajs.dom.CanvasRenderingContext2D

object TextMetrics extends TextMetricsInterface {
  private lazy val offscreenBuffer: CanvasRenderingContext2D = {
    Utils.getCanvasFromElementId("measureBuffer")
  }

  private lazy val replaceSize = """\d+px""".r

  // TODO: Text this regex esp on 1px 1.0px 1.px .1px, what is valid in CSS?
  private lazy val fontSize = """[^\d]*([\d(?:\.\d*)]+)px.*""".r

  private def extractHeight: Double = {
    val fontSize(size) = offscreenBuffer.font
    size.toDouble
  }

  private def swapFont(canvas: CanvasRenderingContext2D, size: Double) = {
    replaceSize.replaceFirstIn(canvas.font, size.toString + "px")
  }

  private[geometry] def withStyle[T](size: Double)(
    f: CanvasRenderingContext2D => T
  ): CanvasRenderingContext2D => T = {
    c =>
      c.textBaseline = "top"
      c.font = swapFont(c, size)
      f(c)
  }

  def measure(text: Text): Extent = {
    withStyle(text.size) { c =>
      Extent(c.measureText(text.s).width, extractHeight)
    }(offscreenBuffer)
  }
}
