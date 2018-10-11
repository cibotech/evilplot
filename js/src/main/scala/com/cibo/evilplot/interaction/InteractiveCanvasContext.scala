package com.cibo.evilplot.interaction

import com.cibo.evilplot.geometry
import com.cibo.evilplot.geometry._
import org.scalajs.dom.raw.{CanvasRenderingContext2D, HTMLCanvasElement, MouseEvent}

import scala.scalajs.js


final case class CanvasInteractionContext(canvas: CanvasRenderingContext2D)
  extends CanvasBasedRenderContext with InteractionMask {

  lazy val xMult: Double = 2 // if the canvas width is scaled within a smaller parent element, typically a scale of 2x is applied
  lazy val yMult: Double = 2 // if the canvas height is scaled within a smaller parent element, typically a scale of 2x is applied

  override protected def getImageData(x: Double, y: Double): Array[Int] = {
    canvas.getImageData(x * xMult, y * yMult, 1, 1).data.toArray
  }

  override def draw(interaction: Interaction): Unit = {
    canvas.beginPath()

    val hex = addEvents(interaction.interactionEvent)

    canvas.fillStyle = s"#$hex"
    interaction.r.draw(this)
    canvas.fillStyle = noInteraction
  }

  // Dont draw anything with color otherwise
  override def draw(style: Style): Unit = {
    style.r.draw(this)
  }

  override def draw(style: StrokeStyle): Unit = {
    style.r.draw(this)
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

