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

package com.cibo.evilplot.plot

import com.cibo.evilplot.colors.{Color, RGB}
import com.cibo.evilplot.geometry.{Disc, Drawable, EmptyDrawable, Extent, Style, Text, Wedge}
import com.cibo.evilplot.numeric.{Bounds, Datum2d, Point, Point2d}
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.renderers.{PathRenderer, PlotRenderer, PointRenderer}
object ScatterPlot extends TransformWorldToScreen {

  case class ScatterPlotRenderer[X <: Datum2d[X]](data: Seq[X],
                                 pointRenderer: PointRenderer[X],
                                ) extends PlotRenderer {

    override def legendContext: LegendContext = pointRenderer.legendContext

    def render(plot: Plot, plotExtent: Extent)(implicit theme: Theme): Drawable = {

      val plotContext = PlotContext(plot, plotExtent)
      val xformedPoints: Seq[X] = transformDatumsToWorld(data, plotContext.xCartesianTransform, plotContext.yCartesianTransform)
      val points = xformedPoints.filter(p => plotExtent.contains(p))
        .flatMap {
          case point =>
            val r = pointRenderer.render(plot, plotExtent, point)
            if (r.isEmpty) None else Some(r.translate(x = point.x, y = point.y))
        }
        .group

      points
    }
  }

  /** Create a scatter plot from some data.
    * @param data The points to plot.
    * @param pointRenderer A function to create a Drawable for each point to plot.
    * @param boundBuffer Extra padding to add to the bounds as a fraction.
    * @return A Plot representing a scatter plot.
    */

  def apply[X <: Datum2d[X]](
    data: Seq[X],
    pointRenderer: Option[PointRenderer[X]] = None,
    xBoundBuffer: Option[Double] = None,
    yBoundBuffer: Option[Double] = None
  )(implicit theme: Theme): Plot = {
    require(xBoundBuffer.getOrElse(0.0) >= 0.0)
    require(yBoundBuffer.getOrElse(0.0) >= 0.0)
    val (xbounds, ybounds) = PlotUtils.bounds(data, theme.elements.boundBuffer, xBoundBuffer, yBoundBuffer)
    Plot(
      xbounds,
      ybounds,
      ScatterPlotRenderer(
        data,
        pointRenderer.getOrElse(PointRenderer.default())
      )
    )
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
      Style(
        Text(name, theme.fonts.legendLabelSize, theme.fonts.fontFace),
        theme.colors.legendLabel),
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
    val pointRenderer = PointRenderer.default[Point](Some(color), pointSize, name)
    ScatterPlot(data, Some(pointRenderer), boundBuffer, boundBuffer)
  }
}
