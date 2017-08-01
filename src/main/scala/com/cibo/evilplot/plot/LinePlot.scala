/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.{Drawable, Extent, Fit, FlipY, Point, Segment}
import org.scalajs.dom.CanvasRenderingContext2D


class LinePlot(override val extent: Extent, data: Seq[Point], options: PlotOptions)
  extends Drawable {
  override def draw(canvas: CanvasRenderingContext2D) = {
    val segment = Fit(extent)(FlipY(Segment(data, strokeWidth = 0.1)))
    segment.draw(canvas)
  }
}
