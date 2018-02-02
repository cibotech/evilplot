package com.cibo.evilplot.geometry

import org.scalajs.dom.CanvasRenderingContext2D

// Run the passed-in rendering function, saving the canvas state before that, and restoring it afterwards.
object CanvasOp {
  def apply(canvas: CanvasRenderingContext2D)(f: => Unit): Unit = {
    canvas.save()
    f
    canvas.restore()
  }
}
