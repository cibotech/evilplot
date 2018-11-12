/*
 * Copyright (c) 2018, CiBO Technologies, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.cibo.evilplot.interaction

import com.cibo.evilplot.geometry
import com.cibo.evilplot.geometry._
import com.cibo.evilplot.numeric.Point
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
                         defaultClick: IEInfo => Unit = _ => (),
                         defaultMove: IEInfo => Unit = _ => ()
                        ): Unit = {

    canvas.addEventListener[MouseEvent]("click", { event: MouseEvent =>

      val canvasY = event.clientY - canvas.getBoundingClientRect().top
      val canvasX = event.clientX - canvas.getBoundingClientRect().left
      events(canvasX, canvasY).find(_.isInstanceOf[OnClick]).map(_.e).getOrElse(defaultClick).apply(
        IEInfo(Point(canvasX, canvasY), Point(event.clientX, event.clientY))
      )
    })

    canvas.addEventListener[MouseEvent]("mousemove", { event: MouseEvent =>

      val canvasY = event.clientY - canvas.getBoundingClientRect().top
      val canvasX = event.clientX - canvas.getBoundingClientRect().left
      events(canvasX, canvasY).find(_.isInstanceOf[OnHover]).map(_.e).getOrElse(defaultMove).apply(
        IEInfo(Point(canvasX, canvasY), Point(event.clientX, event.clientY))
      )
    })
  }
}

