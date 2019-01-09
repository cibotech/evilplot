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
import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent, LineStyle, Rect}
import com.cibo.evilplot.numeric.{Bounds, Datum2d, Point}
import com.cibo.evilplot.plot
import com.cibo.evilplot.plot.BarChart.BarChartRenderer
import com.cibo.evilplot.plot.LinePlot.LinePlotRenderer
import com.cibo.evilplot.plot.ScatterPlot.ScatterPlotRenderer
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.renderers._

sealed abstract class Bin[T] {
  val values: Seq[T]
  def y: Double
}

case class ContinuousBin(values: Seq[Double], bounds: Bounds, y: Double) extends Bin[Double] {
  lazy val x: Bounds = bounds
}

object ContinuousBin {

  def apply(
    values: Seq[Double],
    bounds: Bounds,
    agg: Seq[Double] => Double = _.length): ContinuousBin = {
    new ContinuousBin(values, bounds, agg(values))
  }
}

object Binning {

  def histogramBins(
    seq: Seq[Double],
    ctx: Option[PlotContext],
    numBins: Int = 20,
    normalize: Boolean = false): Seq[ContinuousBin] = {
    ctx match {
      case Some(rctx) =>
        Binning.histogramBinsWithBounds(seq, rctx.xBounds, numBins = numBins, normalize = normalize)
      case None => Binning.histogramBinsDataBounds(seq, numBins = numBins, normalize = normalize)
    }
  }

  def histogramBinsDataBounds(
    seq: Seq[Double],
    numBins: Int = 20,
    normalize: Boolean = false): Seq[ContinuousBin] = {
    histogramBinsWithBounds(seq, Bounds(seq.min, seq.max), numBins, normalize)
  }

  def histogramBinsWithBounds(
    seq: Seq[Double],
    bounds: Bounds,
    numBins: Int = 20,
    normalize: Boolean = false): Seq[ContinuousBin] = {
    val xbounds = bounds //note this is technically the *view* bounds not the bounds of the histogram
    val dataBounds = Bounds.get(seq) getOrElse xbounds
    val binWidth = dataBounds.range / numBins
    val grouped: Map[Int, Seq[Double]] = seq
      .groupBy { value =>
        math.min(((value - dataBounds.min) / binWidth).toInt, numBins - 1)
      }
      .withDefault { i =>
        Seq.empty[Double]
      }

    grouped.toSeq.map {
      case (i, vs) =>
        val x = i * binWidth + dataBounds.min
        ContinuousBin(vs, Bounds(x, x + binWidth))
    }

  }
}

case class BinArgs[T](data: Seq[T], ctx: Option[PlotContext]) {

  def continuousBins(
    extract: T => Double,
    numBins: Int = 20,
    normalize: Boolean = false): Seq[ContinuousBin] = {
    Binning.histogramBins(data.map(extract), ctx, numBins, normalize)
  }

  def continuousBinsWithBounds(
    extract: T => Double,
    bounds: Bounds,
    numBins: Int = 20,
    normalize: Boolean = false): Seq[ContinuousBin] = {
    Binning.histogramBinsWithBounds(data.map(extract), bounds, numBins, normalize)
  }

  def continuousBinsDataBounds(
    extract: T => Double,
    numBins: Int = 20,
    normalize: Boolean = false): Seq[ContinuousBin] = {
    Binning.histogramBinsDataBounds(data.map(extract), numBins, normalize)
  }
}

object BinnedPlot {

  type ContextToDrawableContinuous[T] = ContinuousDataComposer[T] => PlotContext => PlotRenderer

  def continuous[T](
    data: Seq[T],
    binFn: BinArgs[T] => Seq[ContinuousBin],
    xboundBuffer: Option[Double] = None,
    yboundBuffer: Option[Double] = None,
    legendContext: LegendContext = LegendContext())(
    contextToDrawable: ContextToDrawableContinuous[T]*)(implicit theme: Theme): Plot = {
    val bins: Seq[ContinuousBin] = binFn(BinArgs(data, None))

    val xbounds = Bounds.union(bins.map(_.x))
    val ybounds = Bounds(0, bins.map(_.y).max)

    val groupedDataRenderer = ContinuousDataComposer[T](data, binFn)

    Plot(
      xbounds,
      ybounds,
      CompoundPlotRenderer(
        contextToDrawable.map(x => x(groupedDataRenderer)),
        xbounds,
        ybounds,
        legendContext
      )
    )
  }
}

case class ContinuousDataComposer[T](data: Seq[T], binFn: BinArgs[T] => Seq[ContinuousBin]) {

  def manipulate(x: Seq[T] => Seq[T]): ContinuousDataComposer[T] = this.copy(data = x(data))

  def filter(x: T => Boolean): ContinuousDataComposer[T] = this.copy(data.filter(x))

  def histogram(
    barRenderer: Option[ContinuousBinRenderer] = None,
    spacing: Option[Double] = None,
    boundBuffer: Option[Double] = None
  )(pCtx: PlotContext)(implicit theme: Theme): PlotRenderer = {
    val bins = binFn(BinArgs(data, Some(pCtx)))
    Histogram.fromBins(bins, barRenderer, spacing, boundBuffer).renderer
  }

}
