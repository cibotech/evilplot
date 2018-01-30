/*
 * Copyright 2017 CiBO Technologies
 */

package com.cibo.evilplot

import org.scalajs.dom
import org.scalajs.dom.CanvasRenderingContext2D

object Utils {
  val canvas: String = "canvas"
  val measureBuffer: String = "measureBuffer"

  def getCanvasFromElementId(id: String): CanvasRenderingContext2D = {
    // Muuuuuwahahahaha
    dom.window.document.getElementById(id)
      .asInstanceOf[dom.html.Canvas]
      .getContext("2d")
      .asInstanceOf[dom.CanvasRenderingContext2D]
  }

}

