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
import com.cibo.evilplot.numeric.Point
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.renderers.{PathRenderer, PointRenderer}

object LinePlot {

  /** Create a line plot from some data.  Convenience method on top of XyPlot
    *
    * @param data          The points to plot.
    * @param pointRenderer A function to create a Drawable for each point to plot.
    * @param pathRenderer A function to create a Drawable for all the points (such as a path)
    * @param xboundBuffer Extra padding to add to x bounds as a fraction.
    * @param yboundBuffer Extra padding to add to y bounds as a fraction.
    */
  def apply(
    data: Seq[Point],
    pointRenderer: Option[PointRenderer] = None,
    pathRenderer: Option[PathRenderer] = None,
    xboundBuffer: Option[Double] = None,
    yboundBuffer: Option[Double] = None
  )(implicit theme: Theme): Plot = {
    XyPlot(
      data,
      pointRenderer = Some(pointRenderer.getOrElse(PointRenderer.empty())),
      pathRenderer = Some(pathRenderer.getOrElse(PathRenderer.default())),
      xboundBuffer.orElse(Some(0)),
      yboundBuffer
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
  def custom(
    data: Seq[Point],
    pointRenderer: PointRenderer,
    pathRenderer: PathRenderer,
    xboundBuffer: Double,
    yboundBuffer: Double
  )(implicit theme: Theme): Plot = {
    XyPlot(data, Some(pointRenderer), Some(pathRenderer), Some(xboundBuffer), Some(yboundBuffer))
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
    xboundBuffer: Option[Double] = None,
    yboundBuffer: Option[Double] = None
  )(implicit theme: Theme): Plot = {
    val pointRenderer = PointRenderer.empty()
    val pathRenderer = PathRenderer.named(name, color, strokeWidth)
    XyPlot(
      data,
      Some(pointRenderer),
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
  def series(
    data: Seq[Point],
    label: Drawable,
    color: Color,
    strokeWidth: Option[Double],
    xboundBuffer: Option[Double],
    yboundBuffer: Option[Double]
  )(implicit theme: Theme): Plot = {
    val pointRenderer = PointRenderer.empty()
    val pathRenderer = PathRenderer.default(strokeWidth, Some(color), label)
    XyPlot(data, Some(pointRenderer), Some(pathRenderer), xboundBuffer, yboundBuffer)
  }
}
