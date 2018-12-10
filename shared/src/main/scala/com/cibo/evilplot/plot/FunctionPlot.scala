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
import com.cibo.evilplot.geometry.Drawable
import com.cibo.evilplot.numeric.{Bounds, Point}
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.components.FunctionPlotLine
import com.cibo.evilplot.plot.renderers.{PathRenderer, PointRenderer}

object FunctionPlot {
  val defaultBounds: Bounds = Bounds(0, 1)
  val defaultNumPoints: Int = 800

  /** Plot a function.
    *
    * @param function  the function to plot.
    * @param xbounds   Range of values to use as input to the function.
    * @param numPoints Number of values to give to the function.
    */
  def apply(
    function: Double => Double,
    xbounds: Option[Bounds] = None,
    numPoints: Option[Int] = None,
    pathRenderer: Option[PathRenderer[Point]] = None,
    pointRenderer: Option[PointRenderer[Point]] = None,
    xBoundBuffer: Option[Double] = None,
    yBoundBuffer: Option[Double] = None)(implicit theme: Theme): Plot = {
    require(numPoints.forall(_ != 0), "Cannot make a function plot using zero points.")
    val pts = FunctionPlotLine.pointsForFunction(
      function,
      xbounds.getOrElse(defaultBounds),
      numPoints.getOrElse(defaultNumPoints))

    val lineRenderer = pathRenderer.getOrElse(PathRenderer.default())
    val scatterRenderer = pointRenderer.getOrElse(PointRenderer.empty())
    val legendContext = scatterRenderer.legendContext.combine(lineRenderer.legendContext)

    CartesianPlot(pts, xBoundBuffer, yBoundBuffer, legendContext = legendContext)(
      _.line(lineRenderer),
      _.scatter(scatterRenderer)
    )
  }

  /** Plot a function using a name for the legend.
    * @param function  the function to plot
    * @param name      the name to use for the legend
    * @param xbounds   the bounds to plot the function over
    * @param numPoints the number of points to evaluate `function` at
    */
  def series(
    function: Double => Double,
    name: String,
    color: Color,
    xbounds: Option[Bounds] = None,
    numPoints: Option[Int] = None,
    strokeWidth: Option[Double] = None,
    xBoundBuffer: Option[Double] = None,
    yBoundBuffer: Option[Double] = None
  )(implicit theme: Theme): Plot = {
    val renderer = Some(PathRenderer.named[Point](name, color, strokeWidth))
    apply(function, xbounds, numPoints, renderer, None, xBoundBuffer, yBoundBuffer)
  }

  /** Plot a function using a name for the legend.
    * @param function  the function to plot
    * @param label the drawable to use to label this plot in the legend
    * @param xbounds   the bounds to plot the function over
    * @param numPoints the number of points to evaluate `function` at
    */
  def series(
    function: Double => Double,
    label: Drawable,
    color: Color,
    xbounds: Option[Bounds],
    numPoints: Option[Int],
    strokeWidth: Option[Double],
    xBoundBuffer: Option[Double],
    yBoundBuffer: Option[Double])(implicit theme: Theme): Plot = {
    val renderer = Some(PathRenderer.default[Point](strokeWidth, Some(color), label))
    apply(function, xbounds, numPoints, renderer, None, xBoundBuffer, yBoundBuffer)
  }
}
