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
import com.cibo.evilplot.plot.components.{FacetedPlotComponent, Position}
import com.cibo.evilplot.plot.renderers.{ComponentRenderer, PlotRenderer}

/** A plot.
  * @param xbounds The x-axis bounds of the plot.
  * @param ybounds The y-axis bounds of the plot.
  * @param renderer The PlotRenderer used to render the plot area.
  * @param componentRenderer The ComponentRenderer used to render components (axes, labels, backgrounds, etc.).
  * @param xtransform Transformation to convert from plot X-coordinates to pixel coordinates.
  * @param ytransform Transformation to convert from plot Y-coordinates to pixel coordinates.
  * @param xfixed Set if the X bounds are fixed by an axis/grid/facet/etc. or the user.
  * @param yfixed Set if the Y bounds are fixed by an axis/grid/facet/etc. or the user.
  * @param components Plot components (axes, labels, etc.).
  */
final case class Plot(
  xbounds: Bounds,
  ybounds: Bounds,
  renderer: PlotRenderer,
  componentRenderer: ComponentRenderer = ComponentRenderer.Default(),
  xtransform: Plot.Transformer = Plot.DefaultXTransformer(),
  ytransform: Plot.Transformer = Plot.DefaultYTransformer(),
  xfixed: Boolean = false,
  yfixed: Boolean = false,
  components: Seq[FacetedPlotComponent] = Seq.empty
) {
  private[plot] def inBounds(point: Point): Boolean =
    xbounds.isInBounds(point.x) && ybounds.isInBounds(point.y)

  private[plot] def :+(component: FacetedPlotComponent): Plot =
    copy(components = components :+ component)
  private[plot] def +:(component: FacetedPlotComponent): Plot =
    copy(components = component +: components)

  def component(component: FacetedPlotComponent): Plot = this :+ component

  /** Create a copy of this plot with updated x bounds
    * @param newBounds the new bounds.
    */
  def xbounds(newBounds: Bounds): Plot = copy(xbounds = newBounds, xfixed = true)

  /** Create a copy of this plot with updated x bounds
    * @param lower the new minimum x
    * @param upper the new maximum x
    */
  def xbounds(lower: Double = xbounds.min, upper: Double = xbounds.max): Plot =
    xbounds(Bounds(lower, upper))

  /** Create a copy of this plot with updated y bounds
    * @param newBounds the new bounds.
    */
  def ybounds(newBounds: Bounds): Plot = copy(ybounds = newBounds, yfixed = true)

  /** Create a copy of this plot with updated y bounds
    * @param lower the new minimum y
    * @param upper the new maximum y
    */
  def ybounds(lower: Double = ybounds.min, upper: Double = ybounds.max): Plot =
    ybounds(Bounds(lower, upper))

  private[plot] def updateBounds(xb: Bounds, yb: Bounds): Plot = copy(xbounds = xb, ybounds = yb)

  def setXTransform(xt: Plot.Transformer, fixed: Boolean): Plot =
    copy(xtransform = xt, xfixed = fixed)
  def setYTransform(yt: Plot.Transformer, fixed: Boolean): Plot =
    copy(ytransform = yt, yfixed = fixed)

  lazy val topComponents: Seq[FacetedPlotComponent] = components.filter(_.position == Position.Top)
  lazy val bottomComponents: Seq[FacetedPlotComponent] =
    components.filter(_.position == Position.Bottom)
  lazy val leftComponents: Seq[FacetedPlotComponent] =
    components.filter(_.position == Position.Left)
  lazy val rightComponents: Seq[FacetedPlotComponent] =
    components.filter(_.position == Position.Right)
  lazy val backgroundComponents: Seq[FacetedPlotComponent] =
    components.filter(_.position == Position.Background)
  lazy val overlayComponents: Seq[FacetedPlotComponent] =
    components.filter(_.position == Position.Overlay)

  // Get the offset of the plot area.
  private[plot] lazy val plotOffset: Point = componentRenderer.plotOffset(this)

  // Get the size of the actual plot area.
  // Annotations on the left/right reduce the width of the plot area and
  // annotations on the top/bottom reduce the height of the plot area.
  private[plot] def plotExtent(extent: Extent): Extent = {
    components.foldLeft(extent) { (oldExtent, annotation) =>
      val size = annotation.size(this)
      annotation.position match {
        case Position.Top        => oldExtent.copy(height = oldExtent.height - size.height)
        case Position.Bottom     => oldExtent.copy(height = oldExtent.height - size.height)
        case Position.Left       => oldExtent.copy(width = oldExtent.width - size.width)
        case Position.Right      => oldExtent.copy(width = oldExtent.width - size.width)
        case Position.Overlay    => oldExtent
        case Position.Background => oldExtent
      }
    }
  }

  /** Create a Drawable out of this Plot.
    * @param extent the desired size of the resulting Drawable
    */
  def render(extent: Extent = Plot.defaultExtent)(implicit theme: Theme): Drawable = {
    val overlays = componentRenderer.renderFront(this, extent)
    val backgrounds = componentRenderer.renderBack(this, extent)
    val pextent = plotExtent(extent)
    val renderedPlot =
      renderer.render(this, pextent).resize(pextent).translate(x = plotOffset.x, y = plotOffset.y)
    backgrounds behind renderedPlot behind overlays
  }
}

object Plot {
  val defaultExtent: Extent = Extent(800, 600)

  /** Transform coordinates in plot space to pixel space. */
  trait Transformer {
    def apply(plot: Plot, plotExtent: Extent): Double => Double
  }

  final case class DefaultXTransformer() extends Transformer {
    def apply(plot: Plot, plotExtent: Extent): Double => Double = {
      val scale = plotExtent.width / plot.xbounds.range
      (x: Double) =>
        (x - plot.xbounds.min) * scale
    }
  }

  final case class DefaultYTransformer() extends Transformer {
    def apply(plot: Plot, plotExtent: Extent): Double => Double = {
      val scale = plotExtent.height / plot.ybounds.range
      (y: Double) =>
        plotExtent.height - (y - plot.ybounds.min) * scale
    }
  }

  // Add some buffer to the specified bounds.
  private[plot] def expandBounds(bounds: Bounds, buffer: Double): Bounds = {
    require(buffer >= 0.0)
    // For an empty range we update by the buffer amount.
    val amount = if (bounds.range > 0) buffer * bounds.range / 2.0 else buffer / 2.0
    Bounds(bounds.min - amount, bounds.max + amount)
  }

  // Combine the bounds for multiple plots (taking the widest).
  private[plot] def combineBounds(bounds: Seq[Bounds]): Bounds = {
    Bounds(bounds.minBy(_.min).min, bounds.maxBy(_.max).max)
  }

  // Force all plots to have the same size plot area.
  private[plot] def padPlots(
    plots: Seq[Seq[Plot]],
    extent: Extent,
    padRight: Double,
    padBottom: Double
  ): Seq[Seq[Plot]] = {

    // Pad all plots in each column to start at the same x offset.
    val plotOffsets = plots.map(_.map(_.plotOffset))
    val xoffsets = plotOffsets.tail.foldLeft(plotOffsets.head.map(_.x)) { (xs, row) =>
      val common = xs.zip(row).map { case (x, p) => math.max(x, p.x) }
      val extraXs = xs.drop(row.size)
      val rowExtra = row.drop(xs.size).map(_.x)
      common ++ extraXs ++ rowExtra
    }
    val xoffsetPlots = plots.map { row =>
      row.zip(xoffsets).map {
        case (subplot, xoffset) =>
          subplot.padLeft(xoffset - subplot.plotOffset.x)
      }
    }

    // Pad all plots in each row to start at the same y offset.
    val yoffsets = plotOffsets.map(_.maxBy(_.y).y)
    val offsetPlots = xoffsetPlots.zip(yoffsets).map {
      case (row, yoffset) =>
        row.map { subplot =>
          subplot.padTop(yoffset - subplot.plotOffset.y)
        }
    }

    // Now the subplots all start at the same place, so we need to ensure they all
    // end at the same place.  We do this by computing the maximum right and bottom
    // fill amounts and then padding.
    val plotAreas = offsetPlots.flatMap(_.map(_.plotExtent(extent)))
    val minWidth = plotAreas.minBy(_.width).width
    val minHeight = plotAreas.minBy(_.height).height
    val rowCount = offsetPlots.length
    val columnCount = offsetPlots.maxBy(_.length).length
    offsetPlots.zipWithIndex.map {
      case (row, rowIndex) =>
        row.zipWithIndex.map {
          case (subplot, columnIndex) =>
            val extraRight = if (columnIndex + 1 < columnCount) padRight else 0
            val extraBottom = if (rowIndex + 1 < rowCount) padBottom else 0
            val pe = subplot.plotExtent(extent)
            val fillx = pe.width - minWidth + extraRight
            val filly = pe.height - minHeight + extraBottom
            subplot.padRight(fillx).padBottom(filly)
        }
    }
  }
}
