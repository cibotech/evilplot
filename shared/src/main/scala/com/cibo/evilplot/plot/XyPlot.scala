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

import com.cibo.evilplot.geometry._
import com.cibo.evilplot.numeric.{Bounds, Point}
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.renderers.{PathRenderer, PlotRenderer, PointRenderer}

object XyPlot {

  final case class XyPlotRenderer(
    data: Seq[Point],
    pointRenderer: PointRenderer,
    pathRenderer: PathRenderer
  ) extends PlotRenderer {
    override def legendContext: LegendContext =
      pointRenderer.legendContext.combine(pathRenderer.legendContext)
    def render(plot: Plot, plotExtent: Extent)(implicit theme: Theme): Drawable = {
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

      pathRenderer.render(plot, plotExtent, xformedPoints) inFrontOf points
    }
  }

  /** Create an XY plot (ScatterPlot, LinePlot are both special cases) from some data.
    *
    * @param data           The points to plot.
    * @param pointRenderer  A function to create a Drawable for each point to plot.
    * @param pathRenderer   A function to create a Drawable for all the points (such as a path)
    * @param xboundBuffer   Extra padding to add to bounds as a fraction.
    * @param yboundBuffer   Extra padding to add to bounds as a fraction.
    * @return A Plot representing an XY plot.
    */
  def apply(
    data: Seq[Point],
    pointRenderer: Option[PointRenderer] = None,
    pathRenderer: Option[PathRenderer] = None,
    xboundBuffer: Option[Double] = None,
    yboundBuffer: Option[Double] = None
  )(implicit theme: Theme): Plot = {
    require(xboundBuffer.getOrElse(0.0) >= 0.0)
    require(yboundBuffer.getOrElse(0.0) >= 0.0)
    val xs = data.map(_.x)
    val xbuffer = xboundBuffer.getOrElse(theme.elements.boundBuffer)
    val ybuffer = yboundBuffer.getOrElse(theme.elements.boundBuffer)
    val xbounds = Plot.expandBounds(
      Bounds(
        xs.reduceOption[Double](math.min).getOrElse(0.0),
        xs.reduceOption[Double](math.max).getOrElse(0.0)),
      if (data.length == 1 && xbuffer == 0) 0.1 else xbuffer
    )
    val ys = data.map(_.y)
    val ybounds = Plot.expandBounds(
      Bounds(
        ys.reduceOption[Double](math.min).getOrElse(0.0),
        ys.reduceOption[Double](math.max).getOrElse(0.0)),
      if (data.length == 1 && ybuffer == 0) 0.1 else xbuffer
    )
    Plot(
      xbounds,
      ybounds,
      XyPlotRenderer(
        data,
        pointRenderer.getOrElse(PointRenderer.default()),
        pathRenderer.getOrElse(PathRenderer.default()))
    )
  }
}
