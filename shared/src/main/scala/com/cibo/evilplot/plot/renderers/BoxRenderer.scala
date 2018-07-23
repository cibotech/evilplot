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

package com.cibo.evilplot.plot.renderers

import com.cibo.evilplot.colors.{CategoricalColoring, Color}
import com.cibo.evilplot.geometry.{
  Align,
  BorderRect,
  Drawable,
  Extent,
  Line,
  LineDash,
  Rect,
  StrokeStyle
}
import com.cibo.evilplot.numeric.BoxPlotSummaryStatistics
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.renderers.BoxRenderer.BoxRendererContext
import com.cibo.evilplot.plot.{LegendContext, Plot}

trait BoxRenderer extends PlotElementRenderer[BoxRendererContext] {
  def render(plot: Plot, extent: Extent, summary: BoxRendererContext): Drawable
  def legendContext: LegendContext = LegendContext.empty
}

object BoxRenderer {
  final case class BoxRendererContext(
    summaryStatistics: BoxPlotSummaryStatistics,
    index: Int
  )

  def default(
    fillColor: Option[Color] = None,
    strokeColor: Option[Color] = None,
    lineDash: Option[LineDash] = None,
    strokeWidth: Option[Double] = None
  )(implicit theme: Theme): BoxRenderer = new BoxRenderer {
    private val useFillColor = fillColor.getOrElse(theme.colors.fill)
    private val useStrokeColor = strokeColor.getOrElse(theme.colors.path)
    private val useLineDash = lineDash.getOrElse(theme.elements.lineDashStyle)
    private val useStrokeWidth = strokeWidth.getOrElse(theme.elements.strokeWidth)

    def render(
      plot: Plot,
      extent: Extent,
      context: BoxRendererContext
    ): Drawable = {
      val summary = context.summaryStatistics
      val scale = extent.height / (summary.upperWhisker - summary.lowerWhisker)
      val topWhisker = summary.upperWhisker - summary.upperQuantile
      val uppperToMiddle = summary.upperQuantile - summary.middleQuantile
      val middleToLower = summary.middleQuantile - summary.lowerQuantile
      val bottomWhisker = summary.lowerQuantile - summary.lowerWhisker

      Align
        .center(
          StrokeStyle(Line(scale * topWhisker, useStrokeWidth), useStrokeColor)
            .rotated(90),
          BorderRect
            .filled(extent.width, scale * uppperToMiddle)
            .colored(useStrokeColor)
            .filled(useFillColor),
          BorderRect
            .filled(extent.width, scale * middleToLower)
            .colored(useStrokeColor)
            .filled(useFillColor),
          StrokeStyle(Line(scale * bottomWhisker, theme.elements.strokeWidth), useStrokeColor)
            .rotated(90)
        )
        .reduce(_ above _)
    }
  }

  def tufte(
    fillColor: Option[Color] = None,
    strokeColor: Option[Color] = None,
    lineDash: Option[LineDash] = None,
    strokeWidth: Option[Double] = None
  )(implicit theme: Theme): BoxRenderer = new BoxRenderer {

    private val useFillColor = fillColor.getOrElse(theme.colors.fill)
    private val useStrokeColor = strokeColor.getOrElse(theme.colors.path)
    private val useLineDash = lineDash.getOrElse(theme.elements.lineDashStyle)
    private val useStrokeWidth = strokeWidth.getOrElse(theme.elements.strokeWidth)

    def render(plot: Plot, extent: Extent, context: BoxRenderer.BoxRendererContext): Drawable = {
      val summary = context.summaryStatistics
      val scale = extent.height / (summary.upperWhisker - summary.lowerWhisker)
      val topWhisker = summary.upperWhisker - summary.upperQuantile
      val uppperToMiddle = summary.upperQuantile - summary.middleQuantile
      val middleToLower = summary.middleQuantile - summary.lowerQuantile
      val bottomWhisker = summary.lowerQuantile - summary.lowerWhisker

      Align
        .center(
          StrokeStyle(Line(scale * topWhisker, useStrokeWidth / 2), useStrokeColor).rotated(90),
          StrokeStyle(Line(scale * uppperToMiddle, useStrokeWidth), useStrokeColor).rotated(90),
          StrokeStyle(Line(scale * middleToLower, useStrokeWidth), useStrokeColor).rotated(90),
          StrokeStyle(Line(scale * bottomWhisker, useStrokeWidth / 2), useStrokeColor)
            .rotated(90)
        )
        .reduce(_ above _)

    }
  }

  def colorBy[A: Ordering](
    colorDimension: Seq[A],
    fillColoring: Option[CategoricalColoring[A]] = None,
    strokeColor: Option[Color] = None,
    lineDash: Option[LineDash] = None,
    strokeWidth: Option[Double] = None
  )(implicit theme: Theme): BoxRenderer = new BoxRenderer {
    private val useColoring = fillColoring.getOrElse(CategoricalColoring.themed[A])
    private val colorFunc = useColoring(colorDimension)

    def render(plot: Plot, extent: Extent, context: BoxRendererContext): Drawable = {
      BoxRenderer
        .default(fillColor = Some(colorFunc(colorDimension(context.index))))
        .render(plot, extent, context)
    }

    override def legendContext: LegendContext = {
      useColoring.legendContext(colorDimension, legendGlyph = d => Rect(d))
    }
  }
}
