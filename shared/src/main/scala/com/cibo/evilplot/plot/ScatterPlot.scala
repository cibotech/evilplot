package com.cibo.evilplot.plot

import com.cibo.evilplot.colors.Color
import com.cibo.evilplot.geometry.{Drawable, Style, Text}
import com.cibo.evilplot.numeric.Point
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.renderers.{PathRenderer, PointRenderer}

object ScatterPlot {
  /** Create a scatter plot from some data.
    * @param data The points to plot.
    * @param pointRenderer A function to create a Drawable for each point to plot.
    * @param boundBuffer Extra padding to add to the bounds as a fraction.
    * @return A Plot representing a scatter plot.
    */
  def apply(
    data: Seq[Point],
    pointRenderer: Option[PointRenderer] = None,
    boundBuffer: Option[Double] = None
  )(implicit theme: Theme): Plot = {
    XyPlot(data, pointRenderer, Some(PathRenderer.empty()), boundBuffer, boundBuffer)
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
    pointSize: Option[Double] = None,
    boundBuffer: Option[Double] = None
  )(implicit theme: Theme): Plot =
    series(
      data,
      Style(Text(name, theme.fonts.legendLabelSize), theme.colors.legendLabel),
      color,
      pointSize,
      boundBuffer
    )

  /** Create a scatter plot with the specified name and color.
    * @param data The points to plot.
    * @param name The name of this series.
    * @param color The color of the points in this series.
    * @param pointSize The size of points in this series.
    * @param boundBuffer Extra padding to add to bounds as a fraction.
    */
  def series(
    data: Seq[Point],
    name: Drawable,
    color: Color,
    pointSize: Option[Double],
    boundBuffer: Option[Double]
  )(implicit theme: Theme): Plot = {
    val pointRenderer = PointRenderer.default(Some(color), pointSize, name)
    val pathRenderer = PathRenderer.empty()
    XyPlot(data, Some(pointRenderer), Some(pathRenderer), boundBuffer, boundBuffer)
  }
}

