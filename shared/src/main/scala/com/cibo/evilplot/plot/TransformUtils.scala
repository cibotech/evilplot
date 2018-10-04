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
import com.cibo.evilplot.geometry.{
  Clipping,
  Drawable,
  EmptyDrawable,
  Extent,
  LineDash,
  LineStyle,
  Path,
  StrokeStyle
}
import com.cibo.evilplot.numeric.{Datum2d, _}
import com.cibo.evilplot.plot.BoxPlot.makePlot
import com.cibo.evilplot.plot.LinePlot.LinePlotRenderer
import com.cibo.evilplot.plot.ScatterPlot.ScatterPlotRenderer
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.renderers.BoxRenderer.BoxRendererContext
import com.cibo.evilplot.plot.renderers.{BoxRenderer, PathRenderer, PlotRenderer, PointRenderer}
import com.cibo.evilplot.plot.renderers.PathRenderer.calcLegendStrokeLength

trait TransformWorldToScreen {
  type Transformer = Double => Double

  def xCartesianTransformer(xBounds: Bounds, extent: Extent): Double => Double = {
    val scale = extent.width / xBounds.range
    (x: Double) =>
      (x - xBounds.min) * scale
  }

  def yCartesianTransformer(yBounds: Bounds, extent: Extent): Double => Double = {
    val scale = extent.height / yBounds.range
    (y: Double) =>
      { extent.height - (y - yBounds.min) * scale }
  }

  def createTransformers(
    yBounds: Bounds,
    xBounds: Bounds,
    extent: Extent): (Double => Double, Double => Double) = {

    val xtransformer = xCartesianTransformer(xBounds, extent)
    val ytransformer = yCartesianTransformer(yBounds, extent)

    (xtransformer, ytransformer)
  }

  def transformDatumToWorld[T <: Datum2d[T]](
    point: T,
    xtransformer: Transformer,
    ytransformer: Transformer): T = {

    val x = xtransformer(point.x)
    val y = ytransformer(point.y)
    point.withXY(x = x, y = y)
  }

  def transformDatumsToWorld[T <: Datum2d[T]](
    data: Seq[T],
    xtransformer: Transformer,
    ytransformer: Transformer): Seq[T] = {

    data.map(p => transformDatumToWorld(p, xtransformer, ytransformer))
  }

}

object TransformWorldToScreen extends TransformWorldToScreen

case class PlotContext(plot: Plot, extent: Extent) {

  lazy val xBounds: Bounds = plot.xbounds
  lazy val yBounds: Bounds = plot.ybounds

  def xCartesianTransform: Double => Double =
    TransformWorldToScreen.xCartesianTransformer(xBounds, extent)
  def yCartesianTransform: Double => Double =
    TransformWorldToScreen.yCartesianTransformer(yBounds, extent)

  def transformDatumToWorld[X <: Datum2d[X]](point: X): X =
    TransformWorldToScreen.transformDatumToWorld(point, xCartesianTransform, yCartesianTransform)
  def transformDatumsToWorld[X <: Datum2d[X]](points: Seq[X]): Seq[X] =
    points.map(transformDatumToWorld)

}

object PlotContext {
  def from(plot: Plot, extent: Extent): PlotContext = apply(plot, extent)
}

object PlotUtils {

  def boundsWithBuffer(xs: Seq[Double], buffer: Double): Bounds = {

    require(buffer >= 0.0, "boundBuffer cannot be negative")

    val min = xs.reduceOption[Double](math.min).getOrElse(0.0)
    val max = xs.reduceOption[Double](math.max).getOrElse(0.0)

    val bufferValue = (max - min) * buffer

    Bounds(
      min - bufferValue,
      max + bufferValue
    )
  }

  def bounds[T <: Datum2d[T]](
    data: Seq[T],
    defaultBoundBuffer: Double,
    xboundBuffer: Option[Double] = None,
    yboundBuffer: Option[Double] = None): (Bounds, Bounds) = {

    require(xboundBuffer.getOrElse(0.0) >= 0.0, "xboundBuffer cannot be negative")
    require(yboundBuffer.getOrElse(0.0) >= 0.0, "yboundBuffer cannot be negative")

    val xbuffer = xboundBuffer.getOrElse(defaultBoundBuffer)
    val ybuffer = yboundBuffer.getOrElse(defaultBoundBuffer)

    val xs = data.map(_.x)
    val xbounds = boundsWithBuffer(xs, xbuffer)

    val ys = data.map(_.y)
    val ybounds = boundsWithBuffer(ys, ybuffer)

    (xbounds, ybounds)
  }

}
