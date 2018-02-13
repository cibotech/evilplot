package com.cibo.evilplot.plot

import com.cibo.evilplot.colors.Color
import com.cibo.evilplot.numeric.Point
import com.cibo.evilplot.plot.renderers.{PathRenderer, PointRenderer}

object ScatterPlot {
  /** Create a scatter plot from some data.
    * @param data The points to plot.
    * @param pointRenderer A function to create a Drawable for each point to plot.
    * @param boundBuffer Extra padding to add to bounds as a fraction.
    * @return A Plot representing a scatter plot.
    */
  def apply(
    data: Seq[Point],
    pointRenderer: PointRenderer = PointRenderer.default(),
    boundBuffer: Double = XyPlot.defaultBoundBuffer
  ): Plot[Seq[Point]] = {
    XyPlot(data, pointRenderer, pathRenderer = PathRenderer.empty(), boundBuffer)
  }

  /** Create a scatter plot with the specified name and color.
    * @param data The points to plot.
    * @param name The name of this series.
    * @param color The color of the points in this series.
    * @param pointSize The size of points in this series.
    * @param boundBuffer Extra padding to add to bounds as a fraction.
    */
  def series(
    data: Seq[Point],
    name: String,
    color: Color,
    pointSize: Double = PointRenderer.defaultPointSize,
    boundBuffer: Double = XyPlot.defaultBoundBuffer
  ): Plot[Seq[Point]] = {
    val pointRenderer = PointRenderer.default(pointSize, color, Some(name))
    val pathRenderer = PathRenderer.empty()
    XyPlot(data, pointRenderer, pathRenderer, boundBuffer)
  }
}

