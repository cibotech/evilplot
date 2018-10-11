package com.cibo.evilplot.interaction

import com.cibo.evilplot.geometry.{CanvasRenderContext, InteractionEvent, OnClick, OnHover}
import org.scalajs.dom
import org.scalajs.dom.raw.{CanvasRenderingContext2D, HTMLCanvasElement, MouseEvent}
import shapeless.ops.hlist.Unifier

import scala.scalajs.js.timers
import scala.scalajs.js.timers.SetTimeoutHandle

case class MouseEventable(click: Option[() => Unit] = None, mouseover: Option[() => Unit] = None)
trait CanvasInteractionDetection {

  val noInteraction: String = s"#FFFFFF"

  val canvas: CanvasRenderingContext2D

  private var eventListeners: Map[Int, Seq[InteractionEvent]] = Map()

  protected def addEvents(event: Seq[InteractionEvent]) = {
    val key = nextIndexValue
    eventListeners = eventListeners + (key -> event)

    key.toHexString.reverse.padTo(6, "0").reverse.mkString
  }

  def clearEventListeners(): Unit = eventListeners = Map()

  protected def nextIndexValue: Int = {
    (Math.random() * 256 * 256 * 256).toInt
  }

  def events(x: Double, y: Double): Seq[InteractionEvent] = {

    val pixelData = canvas.getImageData(x * 2, y * 2, 1, 1).data
    if(pixelData(3) == 255) { // Filter our alpha < 255 to Prevent aa from impacting the mask

      val idx = (((pixelData(0) * 256 * 256) + (pixelData(1) * 256) + pixelData(2)))

      eventListeners.get(idx).toSeq.flatten
    } else Seq()
  }

  def attachToMainCanvas(canvas: HTMLCanvasElement,
                         defaultClick: () => Unit = () => (),
                         defaultMove: () => Unit = () => ()
                        ): Unit = {

    canvas.addEventListener[MouseEvent]("click", { x =>

      val canvasY = x.clientY - canvas.getBoundingClientRect().top
      val canvasX = x.clientX - canvas.getBoundingClientRect().left
      events(canvasX, canvasY).find(_.isInstanceOf[OnClick]).map(_.e).getOrElse(defaultClick).apply()
    })

    canvas.addEventListener[MouseEvent]("mousemove", { x =>

      val canvasY = x.clientY - canvas.getBoundingClientRect().top
      val canvasX = x.clientX - canvas.getBoundingClientRect().left
      events(canvasX, canvasY).find(_.isInstanceOf[OnHover]).map(_.e).getOrElse(defaultMove).apply()
    })
  }

}

