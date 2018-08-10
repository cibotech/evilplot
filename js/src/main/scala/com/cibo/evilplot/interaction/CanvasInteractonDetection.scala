package com.cibo.evilplot.interaction

import org.scalajs.dom.raw.CanvasRenderingContext2D

case class MouseEventable(click: Option[() => Unit] = None, mouseover: Option[() => Unit] = None)
trait CanvasInteractionDetection {

  val noInteraction: String = s"#FFFFFF"

  val canvas: CanvasRenderingContext2D

  private var eventListeners: Array[MouseEventable] = Array()

  protected def addEvent(event: MouseEventable): String = {
    eventListeners = eventListeners :+ event
    nextIndexValue
  }

  def clearEventListeners(): Unit = eventListeners = Array()

  protected def nextIndexValue: String = {
    val currentNextIndex = eventListeners.length * 10
    currentNextIndex.toHexString.reverse.padTo(6, "0").reverse.mkString
  }

  def events(x: Double, y: Double): Option[MouseEventable] = {

    val pixelData = canvas.getImageData(x * 2, y * 2, 1, 1).data
    if(pixelData(3) == 255) { // Filter our alpha < 255 to Prevent aa from impacting the mask
      println(pixelData(0), pixelData(1), pixelData(2), pixelData(3))

      val idx = (((pixelData(0) * 256 * 256) + (pixelData(1) * 256) + pixelData(2)) - 1) / 10

      if (idx < eventListeners.length && idx >= 0) {
        Some(eventListeners(idx))
      } else None
    } else None
  }

}
