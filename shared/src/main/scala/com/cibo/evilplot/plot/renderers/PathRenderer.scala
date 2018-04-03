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

import com.cibo.evilplot.colors.Color
import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent, Line, Path, StrokeStyle, Style, Text}
import com.cibo.evilplot.numeric.Point
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.components.FunctionPlotLine
import com.cibo.evilplot.plot.{LegendContext, Plot}

trait PathRenderer extends PlotElementRenderer[Seq[Point]] {
  def legendContext: LegendContext = LegendContext.empty
  def render(plot: Plot, extent: Extent, path: Seq[Point]): Drawable
}

object PathRenderer {
  private val legendStrokeLength: Double = 8.0

  /** The default path renderer.
    * @param strokeWidth The width of the path.
    * @param color Point color.
    * @param label A label for this path (for legends).
    */
  def default(
    strokeWidth: Option[Double] = None,
    color: Option[Color] = None,
    label: Drawable = EmptyDrawable()
  )(implicit theme: Theme): PathRenderer = new PathRenderer {
    override def legendContext: LegendContext = label match {
      case _: EmptyDrawable => LegendContext.empty
      case d                => LegendContext.single(
        StrokeStyle(
          Line(
            legendStrokeLength, strokeWidth.getOrElse(theme.elements.strokeWidth)
          ),
          color.getOrElse(theme.colors.path)
        ),
        d
      )
    }

    def render(plot: Plot, extent: Extent, path: Seq[Point]): Drawable = {
      import FunctionPlotLine.plottablePoints
      val plottable = plottablePoints(
        path.sliding(2).flatMap {
          case Seq(p1, p2) => insertEdgePoint(p1, p2, extent)
        }.toVector,
        extent.within
      )

      plottable.map(plottablePath =>
        StrokeStyle(Path(
          plottablePath,
          strokeWidth.getOrElse(theme.elements.strokeWidth)),
          color.getOrElse(theme.colors.path)
        )
      ).group
    }
  }

  /** Path renderer for named paths (to be shown in legends).
    * @param name The name of this path.
    * @param color The color of this path.
    * @param strokeWidth The width of the path.
    */
  def named(
    name: String,
    color: Color,
    strokeWidth: Option[Double] = None
  )(implicit theme: Theme): PathRenderer =
    default(
      strokeWidth,
      Some(color),
      Style(Text(name, theme.fonts.legendLabelSize), theme.colors.legendLabel)
    )

  /** Path renderer for closed paths. The first point is connected to the last point.
    * @param color the color of this path.
    */
  @deprecated("Use the overload taking a strokeWidth, color, and label.", "2 April 2018")
  def closed(color: Color)(implicit theme: Theme): PathRenderer = closed(color = Some(color))

  /** Path renderer for closed paths. The first point is connected to the last point.
    * @param strokeWidth the stroke width
    * @param color the color of the path
    * @param label the label for the legend
    */
  def closed(strokeWidth: Option[Double] = None,
    color: Option[Color] = None,
    label: Drawable = EmptyDrawable())(
    implicit theme: Theme
  ): PathRenderer = new PathRenderer {
    def render(plot: Plot, extent: Extent, path: Seq[Point]): Drawable = {
      path.headOption.fold(EmptyDrawable(): Drawable) { head =>
        default(strokeWidth, color, label).render(plot, extent, path :+ head)
      }
    }
  }

  /**
    * A no-op renderer for when you don't want to render paths (such as on a scatter plot)
    */
  def empty(): PathRenderer = new PathRenderer {
    def render(plot: Plot, extent: Extent, path: Seq[Point]): Drawable = EmptyDrawable()
  }

  private[plot] def clipToBoundary(point: Point, extent: Extent): Point = {
    import math.{max, min}
    Point(min(max(point.x, 0), extent.width), min(max(point.y, 0), extent.height))
  }

  // Insert boundary points where appropriate.
  private[plot] def insertEdgePoint(point1: Point, point2: Point, extent: Extent): Seq[Point] = {
    if (!(extent.within(point1) || extent.within(point2))) {
      Seq.empty[Point]
    } else if (extent.within(point1)) {
      val insert = clipToBoundary(point2, extent)
      Seq(point1, insert, point2)
    } else {
      val insert = clipToBoundary(point1, extent)
      Seq(point2, insert, point1)
    }
  }
}

