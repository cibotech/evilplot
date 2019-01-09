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
import com.cibo.evilplot.geometry.{Drawable, Extent, Interaction, InteractionEvent}
import com.cibo.evilplot.numeric.{Bounds, Datum2d, Point, Point2d}
import com.cibo.evilplot.plot.LinePlot.LinePlotRenderer
import com.cibo.evilplot.plot.ScatterPlot.ScatterPlotRenderer
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.renderers.{PathRenderer, PlotRenderer, PointRenderer}

object LinePlot {

  case class LinePlotRenderer[T <: Datum2d[T]](data: Seq[T], pathRenderer: PathRenderer[T], interactions: Seq[InteractionEvent] = Seq())
      extends PlotRenderer {

    override def legendContext: LegendContext = pathRenderer.legendContext

    def render(plot: Plot, plotExtent: Extent)(implicit theme: Theme): Drawable = {

      val xformedPoints: Seq[T] = PlotContext.from(plot, plotExtent).transformDatumsToWorld(data)

      if(interactions.nonEmpty){
        Interaction(
          pathRenderer.render(plot, plotExtent, xformedPoints), interactions :_*
        )
      } else pathRenderer.render(plot, plotExtent, xformedPoints)
    }

    def withInteraction(interaction: InteractionEvent*): LinePlotRenderer[T] = this.copy(interactions = interaction.toSeq)
  }

  /** Create a line plot from some data.  Convenience method on top of XyPlot
    *
    * @tparam T           The point type
    * @param data         The points to plot.
    * @param pathRenderer A function to create a Drawable for all the points (such as a path)
    * @param xboundBuffer Extra padding to add to x bounds as a fraction.
    * @param yboundBuffer Extra padding to add to y bounds as a fraction.
    */
  def apply[T <: Datum2d[T]](
    data: Seq[T],
    pathRenderer: Option[PathRenderer[T]] = None,
    xBoundBuffer: Option[Double] = None,
    yBoundBuffer: Option[Double] = None
  )(implicit theme: Theme): Plot = {

    val (xbounds, ybounds) =
      PlotUtils.bounds(data, theme.elements.boundBuffer, xBoundBuffer, yBoundBuffer)

    Plot(
      xbounds,
      ybounds,
      LinePlotRenderer(
        data,
        pathRenderer.getOrElse(PathRenderer.default())
      )
    )
  }

  /** Create a line plot from some data.  Convenience method on top of XyPlot
    *
    * @param data          The points to plot.
    * @param pointRenderer A function to create a Drawable for each point to plot.
    * @param pathRenderer A function to create a Drawable for all the points (such as a path)
    * @param xboundBuffer Extra padding to add to x bounds as a fraction.
    * @param yboundBuffer Extra padding to add to y bounds as a fraction.
    */
  @deprecated("Use apply", "2018-03-15")
  def custom[T <: Datum2d[T]](
    data: Seq[T],
    pointRenderer: PointRenderer[T],
    pathRenderer: PathRenderer[T],
    xboundBuffer: Double,
    yboundBuffer: Double
  )(implicit theme: Theme): Plot = {
    LinePlot(data, Some(pathRenderer), Some(xboundBuffer), Some(yboundBuffer))
  }

  /** Create a line plot with the specified name and color.
    * @param data The points to plot.
    * @param name The name of the series.
    * @param color The color of the line.
    * @param strokeWidth The width of the line.
    * @param xboundBuffer Extra padding to add to x bounds as a fraction.
    * @param yboundBuffer Extra padding to add to y bounds as a fraction.
    */
  def series[T <: Datum2d[T]](
    data: Seq[T],
    name: String,
    color: Color,
    strokeWidth: Option[Double] = None,
    xboundBuffer: Option[Double] = None,
    yboundBuffer: Option[Double] = None
  )(implicit theme: Theme): Plot = {
    val pathRenderer = PathRenderer.named[T](name, color, strokeWidth)
    LinePlot(
      data,
      Some(pathRenderer),
      xboundBuffer,
      yboundBuffer
    )
  }

  /** Create a line plot with the specified name and color.
    * @param data The points to plot.
    * @param label A label for this series.
    * @param color The color of the line.
    * @param strokeWidth The width of the line.
    * @param xboundBuffer Extra padding to add to x bounds as a fraction.
    * @param yboundBuffer Extra padding to add to y bounds as a fraction.
    */
  def series[T <: Datum2d[T]](
    data: Seq[T],
    label: Drawable,
    color: Color,
    strokeWidth: Option[Double],
    xboundBuffer: Option[Double],
    yboundBuffer: Option[Double]
  )(implicit theme: Theme): Plot = {
    val pathRenderer = PathRenderer.default[T](strokeWidth, Some(color), label)
    LinePlot(data, Some(pathRenderer), xboundBuffer, yboundBuffer)
  }
}
