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

package com.cibo.evilplot.plot.components

import com.cibo.evilplot.geometry._
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.renderers.LegendRenderer
import com.cibo.evilplot.plot.{LegendContext, Plot}

case class Legend(
  position: Position,
  context: LegendContext,
  legendRenderer: LegendRenderer,
  x: Double,
  y: Double
) extends PlotComponent {

  private lazy val drawable: Drawable = legendRenderer.render(context)

  override def size(plot: Plot): Extent = drawable.extent

  def render(plot: Plot, extent: Extent)(implicit theme: Theme): Drawable = {
    drawable.translate(
      x = (extent.width - drawable.extent.width) * x,
      y = (extent.height - drawable.extent.height) * y
    )
  }
}

trait LegendImplicits {
  protected val plot: Plot

  private def setLegend(
    position: Position,
    renderer: LegendRenderer,
    x: Double,
    y: Double,
    labels: Option[Seq[String]] = None
  )(implicit theme: Theme): Plot = {
    labels match {
      case Some(names) =>
        val drawableLabels: Seq[Drawable] = names.map { value =>
          Style(
            Text(
              value.toString,
              size = theme.fonts.legendLabelSize,
              fontFace = theme.fonts.fontFace),
            theme.colors.legendLabel
          )
        }
        plot :+ Legend(
          position,
          plot.renderer.legendContext.copy(labels = drawableLabels),
          renderer,
          x,
          y)
      case _ =>
        if (plot.renderer.legendContext.nonEmpty) {
          plot :+ Legend(position, plot.renderer.legendContext, renderer, x, y)
        } else {
          plot
        }
    }
  }

  /** Place a legend on the right side of the plot. */
  def rightLegend(
    renderer: LegendRenderer = LegendRenderer.vertical(),
    labels: Option[Seq[String]] = None
  )(implicit theme: Theme): Plot = setLegend(Position.Right, renderer, 0, 0.5, labels)

  /** Place a legend on the left side of the plot. */
  def leftLegend(
    renderer: LegendRenderer = LegendRenderer.vertical(),
    labels: Option[Seq[String]] = None
  )(implicit theme: Theme): Plot = setLegend(Position.Left, renderer, 0, 0.5, labels)

  /** Place a legend on the top of the plot. */
  def topLegend(
    renderer: LegendRenderer = LegendRenderer.horizontal(),
    labels: Option[Seq[String]] = None
  )(implicit theme: Theme): Plot = setLegend(Position.Top, renderer, 0.5, 0, labels)

  /** Place a legend on the bottom of the plot. */
  def bottomLegend(
    renderer: LegendRenderer = LegendRenderer.horizontal(),
    labels: Option[Seq[String]] = None
  )(implicit theme: Theme): Plot = setLegend(Position.Bottom, renderer, 0.5, 0, labels)

  /** Overlay a legend on the plot.
    * @param x The relative X position (0 to 1).
    * @param y The relative y position (0 to 1).
    * @param renderer The legend renderer to use.
    */
  def overlayLegend(
    x: Double = 1.0,
    y: Double = 0.0,
    renderer: LegendRenderer = LegendRenderer.vertical(),
    labels: Option[Seq[String]] = None
  )(implicit theme: Theme): Plot = setLegend(Position.Overlay, renderer, x, y, labels)

  /** Get the legend as a drawable. */
  def renderLegend(
    renderer: LegendRenderer = LegendRenderer.vertical()
  )(implicit theme: Theme): Option[Drawable] =
    if (plot.renderer.legendContext.nonEmpty) {
      val legend = Legend(Position.Right, plot.renderer.legendContext, renderer, 0, 0)
      Some(legend.render(plot, legend.size(plot)))
    } else None
}
