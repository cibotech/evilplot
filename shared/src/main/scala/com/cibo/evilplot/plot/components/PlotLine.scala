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

import com.cibo.evilplot.colors.Color
import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent, Line, LineStyle, Path}
import com.cibo.evilplot.numeric.{Bounds, Point}
import com.cibo.evilplot.plot.Plot
import com.cibo.evilplot.plot.aesthetics.Theme

import scala.annotation.tailrec

sealed trait PlotLine extends PlotComponent {
  val position: Position = Position.Overlay
  override val repeated: Boolean = true
}

case class HorizontalPlotLine(y: Double, thickness: Double, color: Color, lineStyle: LineStyle)
    extends PlotLine {
  def render(plot: Plot, extent: Extent)(implicit theme: Theme): Drawable = {
    val offset = plot.ytransform(plot, extent)(y)
    Line(extent.width, thickness)
      .colored(color)
      .middle()
      .translate(y = offset)
      .dashed(lineStyle)
  }
}

case class VerticalPlotLine(x: Double, thickness: Double, color: Color, lineStyle: LineStyle)
    extends PlotLine {
  def render(plot: Plot, extent: Extent)(implicit theme: Theme): Drawable = {
    val offset = plot.xtransform(plot, extent)(x)
    Line(extent.height, thickness)
      .colored(color)
      .rotated(90)
      .center()
      .translate(x = offset)
      .dashed(lineStyle)
  }
}

case class TrendPlotLine(
  slope: Double,
  intercept: Double,
  color: Color,
  thickness: Double,
  lineStyle: LineStyle)
    extends PlotLine {
  private def solveForX(y: Double): Double = (y - intercept) / slope
  private def valueAt(x: Double): Double = x * slope + intercept

  private def points(plot: Plot): Option[Seq[Point]] = {
    val p1 = Point(solveForX(plot.ybounds.min), plot.ybounds.min)
    val p2 = Point(plot.xbounds.min, valueAt(plot.xbounds.min))
    val p3 = Point(solveForX(plot.ybounds.max), plot.ybounds.max)
    val p4 = Point(plot.xbounds.max, valueAt(plot.xbounds.max))
    val candidates = Seq(p1, p2, p3, p4).filter(plot.inBounds)
    if (candidates.lengthCompare(2) >= 0) {
      Some(Seq(candidates.minBy(_.x), candidates.maxBy(_.x)))
    } else {
      None
    }
  }

  def render(plot: Plot, extent: Extent)(implicit theme: Theme): Drawable = {
    val xtransform = plot.xtransform(plot, extent)
    val ytransform = plot.ytransform(plot, extent)
    points(plot)
      .map { ps =>
        val transformedPoints = ps.map(p => Point(xtransform(p.x), ytransform(p.y)))
        Path(transformedPoints, thickness).colored(color).dashed(lineStyle)
      }
      .getOrElse(EmptyDrawable())
  }
}

case class FunctionPlotLine(
  fn: Double => Double,
  color: Color,
  thickness: Double,
  lineStyle: LineStyle,
  all: Boolean = false)
    extends PlotLine {
  import FunctionPlotLine._

  def render(plot: Plot, extent: Extent)(implicit theme: Theme): Drawable = {
    val xtransform = plot.xtransform(plot, extent)
    val ytransform = plot.ytransform(plot, extent)
    // Try to get decent resolution.
    val numPoints = extent.width.toInt
    val points = pointsForFunction(fn, plot.xbounds, numPoints)

    val paths = if (all) Seq(points) else plottablePoints(points, plot.inBounds)
    paths
      .map { pts =>
        if (pts.nonEmpty) {
          Path(pts.map(p => Point(xtransform(p.x), ytransform(p.y))), thickness)
        } else EmptyDrawable()
      }
      .group
      .colored(color)
  }
}
object FunctionPlotLine {
  // Split up the points into individual paths that are in bounds.
  private[plot] def plottablePoints(
    points: Vector[Point],
    inBounds: Point => Boolean): Seq[Seq[Point]] = {
    @tailrec
    def go(remaining: Vector[Point], acc: Vector[Vector[Point]]): Vector[Vector[Point]] = {
      val dropOutOfBounds = remaining.dropWhile(p => !inBounds(p))
      if (dropOutOfBounds.nonEmpty) {
        val (toPlot, rest) = dropOutOfBounds.span(p => inBounds(p))
        go(rest, acc :+ toPlot)
      } else acc
    }
    go(points, Vector.empty[Vector[Point]])
  }

  private[plot] def pointsForFunction(
    function: Double => Double,
    xbounds: Bounds,
    numPoints: Int
  ): Vector[Point] = {
    // Should give decent resolution.
    val width = xbounds.range / numPoints
    Vector.tabulate(numPoints) { i =>
      val x = xbounds.min + width * i
      Point(x, function(x))
    }
  }
}

trait PlotLineImplicits {
  protected val plot: Plot

  val defaultThickness: Double = 2.0

  def hline(
    y: Double
  )(implicit theme: Theme): Plot =
    plot :+ HorizontalPlotLine(y, defaultThickness, theme.colors.trendLine, LineStyle.Solid)

  def hline(
    y: Double,
    color: Color,
    thickness: Double = defaultThickness,
    lineStyle: LineStyle = LineStyle.Solid
  ): Plot = plot :+ HorizontalPlotLine(y, thickness, color, lineStyle)

  def vline(
    x: Double
  )(implicit theme: Theme): Plot =
    plot :+ VerticalPlotLine(
      x,
      defaultThickness,
      theme.colors.trendLine,
      theme.elements.lineDashStyle)

  def vline(
    x: Double,
    color: Color,
    thickness: Double = defaultThickness,
    lineStyle: LineStyle = LineStyle.Solid
  ): Plot = plot :+ VerticalPlotLine(x, thickness, color, lineStyle)

  def trend(
    slope: Double,
    intercept: Double
  )(implicit theme: Theme): Plot =
    plot :+ TrendPlotLine(
      slope,
      intercept,
      theme.colors.trendLine,
      defaultThickness,
      theme.elements.lineDashStyle)

  def trend(
    slope: Double,
    intercept: Double,
    color: Color,
    thickness: Double = defaultThickness,
    lineStyle: LineStyle = LineStyle.Solid
  ): Plot = plot :+ TrendPlotLine(slope, intercept, color, thickness, lineStyle)

  /** Plot a function. For lines, `trend` is more efficient. */
  def function(fn: Double => Double)(implicit theme: Theme): Plot =
    plot :+ FunctionPlotLine(
      fn,
      theme.colors.trendLine,
      defaultThickness,
      theme.elements.lineDashStyle)

  /** Plot a function. For lines, `trend` is more efficient. */
  def function(
    fn: Double => Double,
    color: Color,
    thickness: Double = defaultThickness,
    lineStyle: LineStyle = LineStyle.Solid
  ): Plot = plot :+ FunctionPlotLine(fn, color, thickness, lineStyle)
}
