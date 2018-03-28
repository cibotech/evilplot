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

import com.cibo.evilplot.geometry.{Drawable, Extent}
import com.cibo.evilplot.numeric._
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.renderers.{PlotRenderer, SurfaceRenderer}

object SurfacePlot {
  private[plot] case class SurfacePlotRenderer(
    data: Seq[Seq[Seq[Point3]]],
    surfaceRenderer: SurfaceRenderer
  ) extends PlotRenderer {

    override def legendContext: LegendContext = surfaceRenderer.legendContext
    def render(plot: Plot, plotExtent: Extent)(implicit theme: Theme): Drawable = {
      val xtransformer = plot.xtransform(plot, plotExtent)
      val ytransformer = plot.ytransform(plot, plotExtent)
      data.map { level =>
        val surface = level.map { path =>
          path.withFilter { p =>
            plot.xbounds.isInBounds(p.x) && plot.ybounds.isInBounds(p.y)
          }.map(p => Point3(xtransformer(p.x), ytransformer(p.y), p.z))
        }
        surfaceRenderer.render(plot, plotExtent, surface)
      }.group
    }
  }
}

object ContourPlot {
  import SurfacePlot._
  val defaultGridDimensions: (Int, Int) = (100, 100)

  def apply(
    data: Seq[Point],
    surfaceRenderer: Option[Seq[Seq[Seq[Point3]]] => SurfaceRenderer] = None,
    gridDimensions: (Int, Int) = defaultGridDimensions,
    contours: Option[Int] = None,
    boundBuffer: Option[Double] = None
  )(implicit theme: Theme): Plot = {

    val contourCount = contours.getOrElse(theme.elements.contours)
    require(contourCount > 0, "Must use at least one contour.")

    val xs = data.map(_.x)
    val xbounds = Plot.expandBounds(
      Bounds(xs.reduceOption[Double](math.min).getOrElse(0.0), xs.reduceOption[Double](math.max).getOrElse(0.0)),
      boundBuffer.getOrElse(theme.elements.boundBuffer)
    )
    val ys = data.map(_.y)
    val ybounds = Plot.expandBounds(
      Bounds(ys.reduceOption[Double](math.min).getOrElse(0.0), ys.reduceOption[Double](math.max).getOrElse(0.0)),
      boundBuffer.getOrElse(theme.elements.boundBuffer)
    )

    val gridData = KernelDensityEstimation.densityEstimate2D(data, gridDimensions, Some(xbounds), Some(ybounds))

    val binWidth = gridData.zBounds.range / contourCount
    val levels = Seq.tabulate(contourCount - 1) { bin =>
      gridData.zBounds.min + (bin + 1) * binWidth
    }

    val contourPaths = MarchingSquares(levels, gridData).zip(levels).map {
      case (levelPaths, level) => levelPaths.map { path =>
        path.map { case Point(x, y) =>
          Point3(x, y, level)
        }
      }
    }

    val srFunction = surfaceRenderer.getOrElse { ps: Seq[Seq[Seq[Point3]]] =>
      SurfaceRenderer.densityColorContours(ps)
    }
    val sr = srFunction(contourPaths)
    Plot(
      xbounds,
      ybounds,
      SurfacePlotRenderer(contourPaths, sr)
    )
  }
}
