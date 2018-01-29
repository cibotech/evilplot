/*
 * Copyright 2017 CiBO Technologies
 */

package com.cibo.evilplot

import com.cibo.evilplot.colors.Color
import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent}
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
  def maybeDrawable[T](value: Option[T])(maker: T => Drawable): Drawable = {
    value match {
      case Some(t) => maker(t)
      case None => EmptyDrawable()
    }
  }

  def createNumericLabel(num: Double, numFrac: Int): String = {
    require(numFrac >= 0 && numFrac <= 20, "JavaScript does not support formatting fewer than 0" +
      s"or more than 20 decimal places, but you attempted to format with $numFrac")
    val fmtString = "%%.%df".format(numFrac)
    fmtString.format(num)
  }

}

