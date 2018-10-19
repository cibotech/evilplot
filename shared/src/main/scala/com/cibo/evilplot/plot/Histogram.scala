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
import com.cibo.evilplot.numeric.{Bounds, Bounds2d, Point}
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.renderers.{BarRenderer, ContinuousBinRenderer, PlotRenderer}

object Histogram {

  val defaultBinCount: Int = 20

  /** Create binCount bins from the given data and xbounds.
    * @param values the raw data
    * @param xbounds the bounds over which to bin
    * @param binCount the number of bins to create
    * @return a sequence of points, where the x coordinates represent the left
    *         edge of the bins and the y coordinates represent their heights
    */
  def createBins(values: Seq[Double], xbounds: Bounds, binCount: Int): Seq[Point] =
    createBins(values, xbounds, binCount, normalize = false, cumulative = false)

  /** Create binCount bins from the given data and xbounds, normalizing the heights
    * such that their sum is 1 */
  def normalize(values: Seq[Double], xbounds: Bounds, binCount: Int): Seq[Point] =
    createBins(values, xbounds, binCount, normalize = true, cumulative = false)

  /** Create binCount bins from the given data and xbounds, cumulatively
    * such that each bin includes the data in all previous bins */
  def cumulative(values: Seq[Double], xbounds: Bounds, binCount: Int): Seq[Point] =
    createBins(values, xbounds, binCount, normalize = false, cumulative = true)

  /** Create binCount bins from the given data and xbounds, computing the bin
    * heights such that they represent the average probability density over each
    * bin interval */
  def density(values: Seq[Double], xbounds: Bounds, binCount: Int): Seq[Point] = {
    val binWidth = xbounds.range / binCount
    createBins(values, xbounds, binCount, normalize = true, cumulative = false)
      .map { case Point(x, y) => Point(x, y / binWidth) }
  }

  /** Create binCount bins from the given data and xbounds, cumulatively
    * such that each bin includes the data in all previous bins, and normalized
    * so that bins approximate a CDF */
  def cumulativeDensity(values: Seq[Double], xbounds: Bounds, binCount: Int): Seq[Point] =
    createBins(values, xbounds, binCount, normalize = true, cumulative = true)

  // Create binCount bins from the given data and xbounds.
  private def createBins(
    values: Seq[Double],
    xbounds: Bounds,
    binCount: Int,
    normalize: Boolean,
    cumulative: Boolean): Seq[Point] = {
    val binWidth = xbounds.range / binCount

    val grouped = values.groupBy { value =>
      math.min(((value - xbounds.min) / binWidth).toInt, binCount - 1)
    }
    val pts = (0 until binCount).map { i =>
      val x = i * binWidth + xbounds.min
      grouped.get(i) match {
        case Some(vs) =>
          val y = if (normalize) vs.size.toDouble / values.size else vs.size
          Point(x, y)
        case _ => Point(x, 0)
      }
    }
    if (cumulative) {
      pts.scanLeft(Point(0, 0)) { case (Point(_, t), Point(x, y)) => Point(x, y + t) }.drop(1)
    } else {
      pts
    }
  }

  @deprecated("Use HistogramBinRenderer instead","v0.6.1")
  case class HistogramRenderer(
    data: Seq[Double],
    barRenderer: BarRenderer,
    binCount: Int,
    spacing: Double,
    boundBuffer: Double,
    binningFunction: (Seq[Double], Bounds, Int) => Seq[Point])
      extends PlotRenderer {
    def render(plot: Plot, plotExtent: Extent)(implicit theme: Theme): Drawable = {
      if (data.nonEmpty) {
        val ctx = PlotCtx(plot, plotExtent, spacing)
        
        val dataBounds = Bounds.get(data) getOrElse plot.xbounds
        val points = binningFunction(data, dataBounds, binCount)
        val binWidth:Double = dataBounds.range/binCount

        val bars = for(p <- points; 
                       xb <- Bounds(p.x, p.x+binWidth) intersect ctx.xbounds;
                       yb <- Bounds(0,   p.y)          intersect ctx.ybounds
                   ) yield {
                      val bar = BoundedBar(xb, yb, ctx)
                      barRenderer.render(plot, bar.extent, Bar(bar.y)).translate(x = bar.x, y = bar.y)
                   }
        bars.group
      } else {
        EmptyDrawable()
      }
    }

    override val legendContext: LegendContext =
      barRenderer.legendContext.getOrElse(LegendContext.empty)

  }

  /** this render assumes the binning of the data has already been applied; i.e in cases where the plot ranges need to be pre-calculated */
  case class HistogramBinRenderer(
    binPoints: Seq[Point],
    binWidth: Double,
    barRenderer: BarRenderer,
    spacing: Double)
      extends PlotRenderer 
  {
    def render(plot: Plot, plotExtent: Extent)(implicit theme: Theme): Drawable = 
      if (binPoints.isEmpty) EmptyDrawable() else {
        val ctx = PlotCtx(plot, plotExtent, spacing)

        //--data bounds
        val xbounds = {
          val leftEdge = Bounds.get(binPoints.map{_.x}) getOrElse Bounds.empty
          Bounds(leftEdge.min, leftEdge.max + binWidth)
        }
        val ybounds = Bounds.get(binPoints.map{_.y} :+ 0d) getOrElse Bounds.empty

        //--constrain to view bounds
        val bars = for(p <- binPoints; 
                       xb <- Bounds(p.x, p.x+binWidth) intersect ctx.xbounds;
                       yb <- Bounds(0,   p.y)          intersect ctx.ybounds
                   ) yield {
                      val bar = BoundedBar(xb, yb, ctx)
                      barRenderer.render(plot, bar.extent, Bar(bar.y)).translate(x = bar.x, y = bar.y)
                   }
        bars.group
     }

    override val legendContext: LegendContext =
      barRenderer.legendContext.getOrElse(LegendContext.empty)
  }

  private case class PlotCtx(plot:Plot, extent:Extent, spacing:Double){
    lazy val xbounds = plot.xbounds
    lazy val ybounds = plot.ybounds

    lazy val tx = plot.xtransform(plot, extent)
    lazy val ty = plot.ytransform(plot, extent)
    lazy val y0 = ty(0)
  }
  private case class BoundedBar(xbin:Bounds,ybin:Bounds, ctx:PlotCtx){
    lazy val x = ctx.tx(xbin.min) + ctx.spacing / 2.0
    lazy val y = ctx.ty(ybin.max)
    lazy val width = ctx.tx(xbin.range) - ctx.spacing
    lazy val height = ctx.y0 - y
    lazy val extent = Extent(width, height)
  }

  case class ContinuousBinPlotRenderer(
    bins: Seq[ContinuousBin],
    binRenderer: ContinuousBinRenderer,
    spacing: Double,
    boundBuffer: Double)
      extends PlotRenderer {

    def render(plot: Plot, plotExtent: Extent)(implicit theme: Theme): Drawable = 
      if (bins.isEmpty) EmptyDrawable() else {

        val ctx = PlotCtx(plot, plotExtent, spacing)

        val bars = for(bin <- bins; 
                      xbin <- bin.x intersect ctx.xbounds;
                      ybin <- Bounds(0,bin.y) intersect ctx.ybounds
                  ) yield {
                    val bar = BoundedBar(xbin, ybin, ctx)
                    binRenderer.render(plot, bar.extent, bin).translate(x = bar.x, y = bar.y)
                  }
        bars.group
      }

    override val legendContext: LegendContext =
      binRenderer.legendContext.getOrElse(LegendContext.empty)

  }

  /** Create a histogram.
    * @param values The data.
    * @param bins The number of bins to divide the data into.
    * @param barRenderer The renderer to render bars for each bin.
    * @param spacing The spacing between bars.
    * @param boundBuffer Extra padding to place at the top of the plot.
    * @param binningFunction A function taking the raw data, the x bounds, and a bin count
    *                        that returns a sequence of points with x points representing left
    *                        bin boundaries and y points representing bin heights
    * @return A histogram plot.
    */
  def apply(
    values: Seq[Double],
    bins: Int = defaultBinCount,
    barRenderer: Option[BarRenderer] = None,
    spacing: Option[Double] = None,
    boundBuffer: Option[Double] = None,
    binningFunction: (Seq[Double], Bounds, Int) => Seq[Point] = createBins)(
    implicit theme: Theme): Plot = 
  {
    require(bins > 0, "must have at least one bin")

    //--merge the multiple sources of configuration options with priority on the arguments over default and themes
    val theBarRenderer = barRenderer.getOrElse(BarRenderer.default())
    val theSpacing = spacing.getOrElse(theme.elements.barSpacing)
    val theBufRatio = boundBuffer.getOrElse(theme.elements.boundBuffer)

    //--auto plot bounds from data. 
    //  Note: the whole histogram shouldn't have to be re-calculated at render time if it is already computed here for the ybounds
    val xbounds = Bounds.get(values) getOrElse Bounds.empty
    val binPoints = binningFunction(values, xbounds, bins)
    val ybounds = Bounds.get(binPoints.map{_.y} :+ 0d).map{_ padMax theBufRatio} getOrElse Bounds.empty
    val binWidth = xbounds.range/bins

    val renderer = HistogramBinRenderer(binPoints, binWidth, theBarRenderer, theSpacing)
    Plot(xbounds, ybounds, renderer)
  }

  def fromBins(
    bins: Seq[ContinuousBin],
    binRenderer: Option[ContinuousBinRenderer] = None,
    spacing: Option[Double] = None,
    boundBuffer: Option[Double] = None)(
    implicit theme: Theme): Plot = 
  {
    require(bins.nonEmpty, "must have at least one bin")

    //view bounds restricting presented(rendered) data
    val bufRatio = boundBuffer getOrElse theme.elements.boundBuffer
    val xBounds = Bounds.union(bins.map(_.x)) //no padding on x
    val yBounds = Bounds.get(bins.map(_.y) :+ 0d).get.padMax(bufRatio) //pad top

    Plot(
      xbounds = xBounds,
      ybounds = yBounds,
      renderer = ContinuousBinPlotRenderer(
        bins,
        binRenderer.getOrElse(ContinuousBinRenderer.default()),
        spacing.getOrElse(theme.elements.barSpacing),
        boundBuffer.getOrElse(theme.elements.boundBuffer)
      )
    )
  }
}
