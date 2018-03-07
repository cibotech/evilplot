package com.cibo.evilplot.plot

import com.cibo.evilplot.colors.Color
import com.cibo.evilplot.geometry.Drawable
import com.cibo.evilplot.numeric.Point
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.renderers.{PathRenderer, PointRenderer}

object LinePlot {
  /** Create a line plot from some data.  Convenience method on top of XyPlot
    *
    * @param data          The points to plot.
    * @param xboundBuffer Extra padding to add to x bounds as a fraction.
    * @param yboundBuffer Extra padding to add to y bounds as a fraction.
    */
  def apply(
    data: Seq[Point],
    xboundBuffer: Double = 0,
    yboundBuffer: Double = XyPlot.defaultBoundBuffer
  )(implicit theme: Theme): Plot = {
    val pointRenderer = PointRenderer.empty()
    val pathRenderer = PathRenderer.default()
    XyPlot(data, pointRenderer, pathRenderer, xboundBuffer, yboundBuffer)
  }

  /** Create a line plot from some data.  Convenience method on top of XyPlot
    *
    * @param data          The points to plot.
    * @param pointRenderer A function to create a Drawable for each point to plot.
    * @param pathRenderer A function to create a Drawable for all the points (such as a path)
    * @param xboundBuffer Extra padding to add to x bounds as a fraction.
    * @param yboundBuffer Extra padding to add to y bounds as a fraction.
    */
  def custom(
    data: Seq[Point],
    pointRenderer: PointRenderer,
    pathRenderer: PathRenderer,
    xboundBuffer: Double,
    yboundBuffer: Double
  )(implicit theme: Theme): Plot = {
    XyPlot(data, pointRenderer, pathRenderer, xboundBuffer, yboundBuffer)
  }

  /** Create a line plot with the specified name and color.
    * @param data The points to plot.
    * @param name The name of the series.
    * @param color The color of the line.
    * @param strokeWidth The width of the line.
    * @param xboundBuffer Extra padding to add to x bounds as a fraction.
    * @param yboundBuffer Extra padding to add to y bounds as a fraction.
    */
  def series(
    data: Seq[Point],
    name: String,
    color: Color,
    strokeWidth: Option[Double] = None,
    xboundBuffer: Double = 0,
    yboundBuffer: Double = XyPlot.defaultBoundBuffer
  )(implicit theme: Theme): Plot = {
    val pointRenderer = PointRenderer.empty()
    val pathRenderer = PathRenderer.named(name, color, strokeWidth)
    XyPlot(data, pointRenderer, pathRenderer, xboundBuffer, yboundBuffer)
  }

  /** Create a line plot with the specified name and color.
    * @param data The points to plot.
    * @param label A label for this series.
    * @param color The color of the line.
    * @param strokeWidth The width of the line.
    * @param xboundBuffer Extra padding to add to x bounds as a fraction.
    * @param yboundBuffer Extra padding to add to y bounds as a fraction.
    */
  def series(
    data: Seq[Point],
    label: Drawable,
    color: Color,
    strokeWidth: Double,
    xboundBuffer: Double,
    yboundBuffer: Double
  )(implicit theme: Theme): Plot = {
    val pointRenderer = PointRenderer.empty()
    val pathRenderer = PathRenderer.default(strokeWidth, color, label)
    XyPlot(data, pointRenderer, pathRenderer, xboundBuffer, yboundBuffer)
  }
}

