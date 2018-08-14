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

import com.cibo.evilplot.colors.Color
import com.cibo.evilplot.geometry.{Disc, Drawable, EmptyDrawable, Extent, Style, Text}
import com.cibo.evilplot.numeric.Point
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.renderers.{PathRenderer, PointRenderer}

object SimpleScatterPlot {

  def transformToDrawSpace(plot: Plot, plotExtent: Extent)(implicit theme: Theme): Drawable = {
    val xtransformer = plot.xtransform(plot, plotExtent)
    val ytransformer = plot.ytransform(plot, plotExtent)
    val xformedPoints = data.map { point =>
      val x = xtransformer(point.x)
      val y = ytransformer(point.y)
      Point(x, y)
    }
    val points = xformedPoints.zipWithIndex
      .withFilter(p => plotExtent.contains(p._1))
      .flatMap {
        case (point, index) =>
          val r = pointRenderer.render(plot, plotExtent, index)
          if (r.isEmpty) None else Some(r.translate(x = point.x, y = point.y))
      }
      .group

    points
  }

  def default(
               color: Option[Color] = None,
               pointSize: Option[Double] = None,
               label: Drawable = EmptyDrawable()
             )(implicit theme: Theme): PointRenderer = new PointRenderer {
    override def legendContext: LegendContext = label match {
      case _: EmptyDrawable => LegendContext.empty
      case d =>
        val size = pointSize.getOrElse(theme.elements.pointSize)
        LegendContext.single(Disc.centered(size).filled(color.getOrElse(theme.colors.point)), d)
    }
    def render(plot: Plot, extent: Extent, index: Int): Drawable = {
      val size = pointSize.getOrElse(theme.elements.pointSize)
      Disc.centered(size).filled(color.getOrElse(theme.colors.point))
    }
  }

  def apply(
             data: Seq[Point],
             pointRenderer: Option[PointRenderer] = None,
             boundBuffer: Option[Double] = None
           )(implicit theme: Theme): Plot = {
    PointPlot(data, pointRenderer, boundBuffer, boundBuffer)
  }
}


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
    val pointRenderer = PointRenderer.default(Some(color), pointSize, name)
    val pathRenderer = PathRenderer.empty()
    XyPlot(data, Some(pointRenderer), Some(pathRenderer), boundBuffer, boundBuffer)
  }
}
