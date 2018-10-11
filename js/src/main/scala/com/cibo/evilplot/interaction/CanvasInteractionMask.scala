package com.cibo.evilplot.interaction

import com.cibo.evilplot.geometry.{CanvasRenderContext, InteractionEvent, OnClick, OnHover}
import org.scalajs.dom
import org.scalajs.dom.raw.{CanvasRenderingContext2D, HTMLCanvasElement, MouseEvent}
import shapeless.ops.hlist.Unifier

import scala.annotation.tailrec
import scala.scalajs.js.timers
import scala.scalajs.js.timers.SetTimeoutHandle

/*
  This provides interaction tracking for a maximum of ~10,000,000 individual elements
   limited by colors in RGB space and likelyhood of collision
 */
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

  @tailrec
  protected final def nextIndexValue: Int = {

    val next = (Math.random() * 256 * 256 * 256).toInt
    if(eventListeners.keys.exists(_ == next)){
      nextIndexValue
    } else next
  }

  def events(x: Double, y: Double): Seq[InteractionEvent] = {

    val pixelData = canvas.getImageData(x * 2, y * 2, 1, 1).data
    if(pixelData(3) == 255) { // only match non-alpha colors

      // calcuate idx from rgb
      val idx = (((pixelData(0) * 256 * 256) + (pixelData(1) * 256) + pixelData(2)))

      eventListeners.get(idx).toSeq.flatten
    } else Seq()
  }

  /*
    Map canvas interaction events to internal eventables
    Event locations are in window space, map them to their location within the canvas
    */
  def attachToMainCanvas(canvas: HTMLCanvasElement,
                         defaultClick: () => Unit = () => (),
                         defaultMove: () => Unit = () => ()
                        ): Unit = {

    canvas.addEventListener[MouseEvent]("click", { event =>

      val canvasY = event.clientY - canvas.getBoundingClientRect().top
      val canvasX = event.clientX - canvas.getBoundingClientRect().left
      events(canvasX, canvasY).find(_.isInstanceOf[OnClick]).map(_.e).getOrElse(defaultClick).apply()
    })

    canvas.addEventListener[MouseEvent]("mousemove", { event =>

      val canvasY = event.clientY - canvas.getBoundingClientRect().top
      val canvasX = event.clientX - canvas.getBoundingClientRect().left
      events(canvasX, canvasY).find(_.isInstanceOf[OnHover]).map(_.e).getOrElse(defaultMove).apply()
    })
  }

}

