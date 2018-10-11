package com.cibo.evilplot.interaction

import com.cibo.evilplot.geometry
import com.cibo.evilplot.geometry._
import org.scalajs.dom.raw.CanvasRenderingContext2D


final case class InteractionMaskContext(canvas: CanvasRenderingContext2D)
  extends CanvasBasedRenderContext with CanvasInteractionDetection {

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
}

