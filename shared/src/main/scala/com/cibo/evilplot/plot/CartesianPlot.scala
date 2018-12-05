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
import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent, Gradient2d, GradientFill, InteractionEvent, LineStyle}
import com.cibo.evilplot.numeric.{Bounds, BoxPlotSummaryStatistics, Datum2d, Point}
import com.cibo.evilplot.plot.LinePlot.LinePlotRenderer
import com.cibo.evilplot.plot.ScatterPlot.ScatterPlotRenderer
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.renderers.BoxRenderer.BoxRendererContext
import com.cibo.evilplot.plot.renderers._

object CartesianPlot {

  type ContextToDrawable[T <: Datum2d[T]] = CartesianDataComposer[T] => PlotContext => PlotRenderer

  def apply[T <: Datum2d[T]](
    data: Seq[T],
    xboundBuffer: Option[Double] = None,
    yboundBuffer: Option[Double] = None,
    legendContext: LegendContext = LegendContext()
  )(contextToDrawable: ContextToDrawable[T]*)(implicit theme: Theme): Plot = {

    val (xbounds, ybounds) =
      PlotUtils.bounds(data, theme.elements.boundBuffer, xboundBuffer, yboundBuffer)

    val cartesianDataRenderer = CartesianDataComposer(data)

    Plot(
      xbounds,
      ybounds,
      CompoundPlotRenderer(
        contextToDrawable.map(x => x(cartesianDataRenderer)),
        xbounds,
        ybounds,
        legendContext
      )
    )
  }
}

case class CartesianDataComposer[T <: Datum2d[T]](data: Seq[T], pathInteractions: Seq[InteractionEvent] = Seq()) {

  def manipulate(x: Seq[T] => Seq[T]): CartesianDataComposer[T] = this.copy( data = x(data))

  def filter(x: T => Boolean): CartesianDataComposer[T] = this.copy(data = data.filter(x))

  def reducePoint(reducer: T => Double): CartesianDataComposer[Point] = this.copy(data.map(datum => Point(datum.x, reducer(datum))))

  // appends data
  def appendData(toAppend: Seq[T]): CartesianDataComposer[T] = this.copy(data ++ toAppend)

  def withPathInteraction(interaction: Seq[InteractionEvent]): CartesianDataComposer[T] = this.copy(pathInteractions = interaction)

  // appends data, and closes with the head of the existing data
  def appendDataAndClosePath(toAppend: Seq[T]): CartesianDataComposer[T] = {
    if(data.isEmpty) this.copy(toAppend)
    else this.copy(data ++ toAppend :+ data.head)
  }

  def scatter(pointToDrawable: T => Drawable, legendCtx: LegendContext = LegendContext.empty)(
    pCtx: PlotContext)(implicit theme: Theme): PlotRenderer = {
    ScatterPlotRenderer(data, PointRenderer.custom(pointToDrawable, Some(legendCtx)))
  }

  def scatter(pCtx: PlotContext)(implicit theme: Theme): ScatterPlotRenderer[T] = {
    ScatterPlotRenderer(data, PointRenderer.default())
  }

  def scatter(pointRenderer: PointRenderer[T])(pCtx: PlotContext)(
    implicit theme: Theme): ScatterPlotRenderer[T] = {
    ScatterPlotRenderer(data, pointRenderer)
  }

  def line(
    strokeWidth: Option[Double] = None,
    color: Option[Color] = None,
    label: Drawable = EmptyDrawable(),
    lineStyle: Option[LineStyle] = None,
    legendCtx: LegendContext = LegendContext.empty
  )(pCtx: PlotContext)(implicit theme: Theme): LinePlotRenderer[T] = {
    LinePlotRenderer(data, PathRenderer.default(strokeWidth, color, label, lineStyle)).withInteraction(pathInteractions:_*)
  }

  def line(color: Color)(pCtx: PlotContext)(implicit theme: Theme): LinePlotRenderer[T] = {
    LinePlotRenderer(data, PathRenderer.default(color = Some(color))).withInteraction(pathInteractions:_*)
  }

  def line(pCtx: PlotContext)(implicit theme: Theme): LinePlotRenderer[T] = {
    LinePlotRenderer(data, PathRenderer.default()).withInteraction(pathInteractions:_*)
  }

  def line(pathRenderer: PathRenderer[T])(pCtx: PlotContext)(
    implicit theme: Theme): LinePlotRenderer[T] = {
    LinePlotRenderer(data, pathRenderer).withInteraction(pathInteractions:_*)
  }

  def areaToYBound(fill: Color,
                 lineColor: Option[Color] = None,
                 fillToY: Option[Double] = None)(pCtx: PlotContext)(implicit theme: Theme): LinePlotRenderer[T] = {

    LinePlotRenderer(data, PathRenderer.filled(fill, lineColor, fillToY)).withInteraction(pathInteractions:_*)
  }

  def areaToYmin(pCtx: PlotContext)(implicit theme: Theme): LinePlotRenderer[T] = {
    LinePlotRenderer(data, PathRenderer.filled(theme.colors.path)).withInteraction(pathInteractions:_*)
  }

  def areaGradientToYBound(fill: PlotContext => Gradient2d,
                           lineColor: Option[Color] = None,
                           fillToY: Option[Double] = None)(pCtx: PlotContext)(implicit theme: Theme): LinePlotRenderer[T] = {

    LinePlotRenderer(data, PathRenderer.filledGradient(fill, lineColor, fillToY)).withInteraction(pathInteractions:_*)
  }

  def areaGradientSelfClosing(fill: PlotContext => Gradient2d,
                      lineColor: Option[Color] = None)(pCtx: PlotContext)(implicit theme: Theme): LinePlotRenderer[T] = {

    LinePlotRenderer(data, PathRenderer.filledGradientSelfClosing(fill, lineColor)).withInteraction(pathInteractions:_*)
  }

  def areaSelfClosing(fill: Color,
                      lineColor: Option[Color] = None)(pCtx: PlotContext)(implicit theme: Theme): LinePlotRenderer[T] = {

    LinePlotRenderer(data, PathRenderer.filledSelfClosing(fill, lineColor)).withInteraction(pathInteractions:_*)
  }
}
