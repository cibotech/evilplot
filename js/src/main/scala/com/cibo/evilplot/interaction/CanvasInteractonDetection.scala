package com.cibo.evilplot.interaction

import org.scalajs.dom.raw.CanvasRenderingContext2D

case class MouseEventable(click: Option[() => Unit] = None, mouseover: Option[() => Unit] = None)
trait CanvasInteractionDetection {

  val noInteraction: String = s"#FFFFFF"

  val canvas: CanvasRenderingContext2D

  private var eventListeners: Map[Int, MouseEventable] = Map()

  protected def addEvent(event: MouseEventable) = {
    val key = nextIndexValue
    eventListeners = eventListeners + (key -> event)

    key.toHexString.reverse.padTo(6, "0").reverse.mkString
  }

  def clearEventListeners(): Unit = eventListeners = Map()

  protected def nextIndexValue: Int = {
    (Math.random() * 256 * 256 * 256).toInt
  }

  def events(x: Double, y: Double): Option[MouseEventable] = {

    val pixelData = canvas.getImageData(x * 2, y * 2, 1, 1).data
    if(pixelData(3) == 255) { // Filter our alpha < 255 to Prevent aa from impacting the mask

      val idx = (((pixelData(0) * 256 * 256) + (pixelData(1) * 256) + pixelData(2)))
      println(pixelData(0), pixelData(1), pixelData(2), pixelData(3))
      println(idx)

      eventListeners.get(idx)
    } else None
  }

}
