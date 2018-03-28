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

import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent}
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.numeric.Point
import com.cibo.evilplot.plot.components.Position
import com.cibo.evilplot.plot.renderers.{ComponentRenderer, PlotRenderer}

object Facets {

  type FacetData = Seq[Seq[Plot]]

  // Divide the plotExtent evenly among subplots.
  private def computeSubplotExtent(plot: Plot, subplots: FacetData, plotExtent: Extent): Extent = {
    val rows = subplots.size
    val cols = subplots.map(_.size).max
    Extent(plotExtent.width / cols, plotExtent.height / rows)
  }

  // Add padding to subplots so they are all the same size and use the same axis transformation.
  private def updatePlotsForFacet(plot: Plot, subplots: FacetData, subplotExtent: Extent): FacetData = {
    Plot.padPlots(subplots, subplotExtent, 20, 15).map { row =>
      row.map { subplot =>
        val withX = if (subplot.xfixed) subplot else subplot.setXTransform(plot.xtransform, fixed = false)
        if (withX.yfixed) withX else withX.setYTransform(plot.ytransform, fixed = false)
      }
    }
  }

  private case class FacetedPlotRenderer(subplots: FacetData) extends PlotRenderer {
    override def legendContext: LegendContext = LegendContext.combine(subplots.flatMap(_.map(_.renderer.legendContext)))
    def render(plot: Plot, plotExtent: Extent)(implicit theme: Theme): Drawable = {

      // Make sure all subplots have the same size plot area.
      val innerExtent = computeSubplotExtent(plot, subplots, plotExtent)
      val paddedPlots = updatePlotsForFacet(plot, subplots, innerExtent)

      // Render the plots.
      paddedPlots.zipWithIndex.map { case (row, yIndex) =>
        val y = yIndex * innerExtent.height
        row.zipWithIndex.map { case (subplot, xIndex) =>
          val x = xIndex * innerExtent.width
          subplot.render(innerExtent).translate(x = x, y = y)
        }.group
      }.group
    }
  }

  private case class FacetedComponentRenderer(data: FacetData) extends ComponentRenderer {

    private val empty: Drawable = EmptyDrawable()

    private def renderTop(
      plot: Plot,
      subplots: FacetData,
      extent: Extent,
      innerExtent: Extent
    )(implicit theme: Theme): Drawable = {
      plot.topComponents.reverse.foldLeft(empty) { (d, c) =>
        if (c.repeated) {
          val componentHeight = subplots.head.map(subplot => c.size(subplot).height).max
          subplots.head.zipWithIndex.map { case (subplot, i) =>
            val componentExtent = subplot.plotExtent(innerExtent).copy(height = componentHeight)
            val x = i * innerExtent.width + subplot.plotOffset.x + plot.plotOffset.x
            val y = d.extent.height
            c.render(subplot, componentExtent, 0, i).translate(x = x, y = y)
          }.group behind d
        } else {
          val minExtent = c.size(plot)
          val componentExtent = plot.plotExtent(extent).copy(height = minExtent.height)
          val x = plot.plotOffset.x
          val y = d.extent.height
          c.render(plot, componentExtent, 0, 0).translate(x = x, y = y) behind d
        }
      }
    }

    private def renderBottom(
      plot: Plot,
      subplots: FacetData,
      extent: Extent,
      innerExtent: Extent
    )(implicit theme: Theme): Drawable = {
      val startY = extent.height
      val bottomRowIndex = subplots.size - 1
      plot.bottomComponents.reverse.foldLeft((startY, empty)) { case ((prevY, d), c) =>
        if (c.repeated) {
          val componentHeight = subplots.last.map(subplot => c.size(subplot).height).max
          val s = subplots.last.zipWithIndex.map { case (subplot, i) =>
            val componentExtent = subplot.plotExtent(innerExtent).copy(height = componentHeight)
            val rendered = c.render(subplot, componentExtent, bottomRowIndex, i)
            val x = i * innerExtent.width + subplot.plotOffset.x + plot.plotOffset.x
            val y = prevY - rendered.extent.height
            (y, rendered.translate(x = x, y = y))
          }
          (s.maxBy(_._1)._1, s.map(_._2).group behind d)
        } else {
          val minExtent = c.size(plot)
          val componentExtent = plot.plotExtent(extent).copy(height = minExtent.height)
          val rendered = c.render(plot, componentExtent, 0, 0)
          val x = plot.plotOffset.x
          val y = prevY - rendered.extent.height
          (y, rendered.translate(x = x, y = y) behind d)
        }
      }._2
    }

    private def renderLeft(
      plot: Plot,
      subplots: FacetData,
      extent: Extent,
      innerExtent: Extent
    )(implicit theme: Theme): Drawable = {
      lazy val leftPlots = subplots.map(_.head)
      plot.leftComponents.foldLeft(empty) { (d, c) =>
        if (c.repeated) {
          val componentWidth = leftPlots.map(subplot => c.size(subplot).width).max
          leftPlots.zipWithIndex.map { case (subplot, i) =>
            val componentExtent = subplot.plotExtent(innerExtent).copy(width = componentWidth)
            val y = i * innerExtent.height + subplot.plotOffset.y + plot.plotOffset.y
            c.render(subplot, componentExtent, i, 0).translate(y = y)
          }.group beside d
        } else {
          val minExtent = c.size(plot)
          val componentExtent = plot.plotExtent(extent).copy(width = minExtent.width)
          val y = plot.plotOffset.y
          c.render(plot, componentExtent, 0, 0).translate(y = y) beside d
        }
      }
    }

    private def renderRight(
      plot: Plot,
      subplots: FacetData,
      extent: Extent,
      innerExtent: Extent
    )(implicit theme: Theme): Drawable = {
      val rightPlots = subplots.map(_.last)
      val startX = extent.width
      plot.rightComponents.reverse.foldLeft((startX, empty)) { case ((prevX, d), c) =>
        if (c.repeated) {
          val componentWidth = rightPlots.map(subplot => c.size(subplot).width).max
          val s = rightPlots.zipWithIndex.map { case (subplot, i) =>
            val minExtent = c.size(subplot)
            val componentExtent = subplot.plotExtent(innerExtent).copy(width = componentWidth)
            val rendered = c.render(subplot, componentExtent, i, subplots(i).size - 1)
            val x = prevX - rendered.extent.width
            val y = i * innerExtent.height + subplot.plotOffset.y + plot.plotOffset.y
            (x, rendered.translate(x, y))
          }
          (s.maxBy(_._1)._1, s.map(_._2).group behind d)
        } else {
          val minExtent = c.size(plot)
          val componentExtent = plot.plotExtent(extent).copy(width = minExtent.width)
          val rendered = c.render(plot, componentExtent, 0, 0)
          val x = prevX - rendered.extent.width
          val y = plot.plotOffset.y
          (x, rendered.translate(x = x, y = y) behind d)
        }
      }._2
    }

    private def renderGrid(
      position: Position,
      plot: Plot,
      subplots: FacetData,
      extent: Extent,
      innerExtent: Extent
    )(implicit theme: Theme): Drawable = {
      plot.components.filter(_.position == position).map { c =>
        if (c.repeated) {
          subplots.zipWithIndex.flatMap { case (row, yIndex) =>
            row.zipWithIndex.map { case (subplot, xIndex) =>
              val plotExtent = subplot.plotExtent(innerExtent)
              val x = xIndex * innerExtent.width + subplot.plotOffset.x
              val y = yIndex * innerExtent.height + subplot.plotOffset.y
              c.render(subplot, plotExtent, xIndex, yIndex).translate(x = x, y = y)
            }
          }.group
        } else {
          val plotExtent = plot.plotExtent(extent)
          c.render(plot, plotExtent, 0, 0)
        }
      }.group.translate(x = plot.plotOffset.x, y = plot.plotOffset.y)
    }

    def renderFront(plot: Plot, extent: Extent)(implicit theme: Theme): Drawable = {
      val plotExtent = plot.plotExtent(extent)
      val innerExtent = computeSubplotExtent(plot, data, plotExtent)
      val paddedPlots = updatePlotsForFacet(plot, data, innerExtent)
      val top = renderTop(plot, paddedPlots, extent, innerExtent)
      val bottom = renderBottom(plot, paddedPlots, extent, innerExtent)
      val left = renderLeft(plot, paddedPlots, extent, innerExtent)
      val right = renderRight(plot, paddedPlots, extent, innerExtent)
      val overlay = renderGrid(Position.Overlay, plot, paddedPlots, extent, innerExtent)
      Seq(top, bottom, left, right, overlay).group
    }

    def renderBack(plot: Plot, extent: Extent)(implicit theme: Theme): Drawable = {
      val plotExtent = plot.plotExtent(extent)
      val innerExtent = computeSubplotExtent(plot, data, plotExtent)
      val paddedPlots = updatePlotsForFacet(plot, data, innerExtent)
      renderGrid(Position.Background, plot, paddedPlots, extent, innerExtent)
    }

    def plotOffset(plot: Plot): Point = {
      val xoffset = data.map { ps =>
        plot.leftComponents.map(_.size(ps.head).width).sum
      }.foldLeft(0.0)(math.max)
      val yoffset = data.head.map { p =>
        plot.topComponents.map(_.size(p).height).sum
      }.foldLeft(0.0)(math.max)
      Point(xoffset, yoffset)
    }
  }

  def apply(plots: Seq[Seq[Plot]]): Plot = {
    require(plots.nonEmpty, "Facets must include at least one plot")

    // X bounds for each column.
    val columnXBounds = plots.tail.foldLeft(plots.head.map(_.xbounds)) { (bounds, row) =>
      val common = bounds.zip(row).map { case (b, r) => Plot.combineBounds(Seq(b, r.xbounds)) }
      val extraBounds = bounds.drop(row.size)
      val extraRows = row.drop(bounds.size).map(_.xbounds)
      common ++ extraBounds ++ extraRows
    }

    // Y bounds for each row.
    val rowYBounds = plots.map(row => Plot.combineBounds(row.map(_.ybounds)))

    // Update bounds on subplots for subplots that don't already have axes.
    val updatedPlots = plots.zipWithIndex.map { case (row, y) =>
      row.zipWithIndex.map { case (subplot, x) =>
        (subplot.xfixed, subplot.yfixed) match {
          case (true, true)   => subplot
          case (true, false)  => subplot.updateBounds(subplot.xbounds, rowYBounds(y))
          case (false, true)  => subplot.updateBounds(columnXBounds(x), subplot.ybounds)
          case (false, false) => subplot.updateBounds(columnXBounds(x), rowYBounds(y))
        }
      }
    }

    Plot(
      xbounds = Plot.combineBounds(columnXBounds),
      ybounds = Plot.combineBounds(rowYBounds),
      renderer = FacetedPlotRenderer(updatedPlots),
      componentRenderer = FacetedComponentRenderer(updatedPlots)
    )
  }
}

