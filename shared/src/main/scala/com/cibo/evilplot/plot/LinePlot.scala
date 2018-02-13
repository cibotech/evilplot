package com.cibo.evilplot.plot

import com.cibo.evilplot.colors.Color
import com.cibo.evilplot.numeric.Point
import com.cibo.evilplot.plot.renderers.{PathRenderer, PointRenderer}

object LinePlot {
  /** Create a line plot from some data.  Convenience method on top of XyPlot
    *
    * @param data          The points to plot.
    * @param pointRenderer A function to create a Drawable for each point to plot.
    * @param pathRenderer A function to create a Drawable for all the points (such as a path)
    * @param boundBuffer   Extra padding to add to bounds as a fraction.
    */
  def apply(
    data: Seq[Point],
    pointRenderer: PointRenderer[Seq[Point]] = PointRenderer.empty(),
    pathRenderer: PathRenderer = PathRenderer.default(),
    boundBuffer: Double = XyPlot.defaultBoundBuffer
  ): Plot[Seq[Point]] = {
    XyPlot(data, pointRenderer, pathRenderer, boundBuffer)
  }

  /** Create a line plot with the specified name and color.
    * @param data The points to plot.
    * @param name The name of the series.
    * @param color The color of the line.
    */
  def series(
    data: Seq[Point],
    name: String,
    color: Color,
    strokeWidth: Double = PathRenderer.defaultStrokeWidth,
    boundBuffer: Double = XyPlot.defaultBoundBuffer
  ): Plot[Seq[Point]] = {
    val pointRenderer = PointRenderer.empty[Seq[Point]]()
    val pathRenderer = PathRenderer.named(name, color, strokeWidth)
    XyPlot(data, pointRenderer, pathRenderer, boundBuffer)
  }
}

