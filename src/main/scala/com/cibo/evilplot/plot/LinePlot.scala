/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.plot

import com.cibo.evilplot.StrokeStyle
import com.cibo.evilplot.colors.Color
import com.cibo.evilplot.geometry.{Drawable, Extent, FlipY, Group, Path, Point, Scale}
import org.scalajs.dom.CanvasRenderingContext2D


// Plot paths connecting the points in data. Each Seq[Point] makes a path, drawn using the corresponding color.
// TODO: use a layout manager to abstract out common logic
class LinePlot(override val extent: Extent, data: Seq[Seq[Point]], colors: Seq[Color], options: PlotOptions)
  extends Drawable {

  private val _drawable: Drawable = {
    require(data.length == colors.length)

    val paths: Seq[Drawable] = (data zip colors).map { case (_data: Seq[Point], color: Color) =>
      FlipY(StrokeStyle(color)(Path(_data, strokeWidth = 0.1)))
    }
    val group = Group(paths: _*)

    // Scale to fit all the paths.
    // TODO: don't assume that the x size dominates, factor in the y size as well.
    val xvals: Seq[Double] = data.flatMap(_.map(_.x))
    val xMin: Double = xvals.reduce[Double](math.min)
    val xMax: Double = xvals.reduce[Double](math.max)
    val xscale = extent.width / (xMax - xMin)
    Scale(xscale, xscale)(group)
  }

  override def draw(canvas: CanvasRenderingContext2D): Unit = _drawable.draw(canvas)
}
