package com.cibo.evilplot.plot

import com.cibo.evilplot.colors.Color
import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent, LineStyle}
import com.cibo.evilplot.numeric.{Bounds, Datum2d, Point}
import com.cibo.evilplot.plot
import com.cibo.evilplot.plot.BarChart.BarChartRenderer
import com.cibo.evilplot.plot.LinePlot.LinePlotRenderer
import com.cibo.evilplot.plot.ScatterPlot.ScatterPlotRenderer
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.renderers.{BarRenderer, PathRenderer, PlotRenderer, PointRenderer}

import scala.reflect.ClassTag

final case class CompoundPlotRenderer(drawablesToPlot: Seq[PlotContext => PlotRenderer],
                                      xBounds: Bounds,
                                      yBounds: Bounds) extends PlotRenderer {
  override def legendContext: LegendContext = LegendContext()

  def render(plot: Plot, plotExtent: Extent)(implicit theme: Theme): Drawable = {
    drawablesToPlot.foldLeft(EmptyDrawable() : Drawable){ case (accum, dr) =>
      dr(PlotContext(plot, plotExtent)).render(plot, plotExtent) behind accum
    }
  }
}

sealed abstract class Bin[T] {
  val values: Seq[T]
  def y: Double
}

case class ContinuousBin(values: Seq[Double], bounds: Bounds, agg: Seq[Double] => Double = _.length) extends Bin[Double] {
  lazy val x: Bounds = bounds
  lazy val y: Double = agg(values)
}

object Binning {

  def histogramBinsWithPlotBounds(seq: Seq[Double],
                                  ctx: Option[PlotContext],
                                  numBins: Int = 20,
                                  normalize: Boolean = false): Seq[ContinuousBin] = {
    ctx match {
      case Some(rctx) => Binning.histogramBinsWithBounds(seq, rctx.xBounds)
      case None => Binning.histogramBins(seq)
    }
  }

  def histogramBins(seq: Seq[Double], numBins: Int = 20, normalize: Boolean = false): Seq[ContinuousBin] = {
    histogramBinsWithBounds(seq, Bounds(seq.min, seq.max), numBins, normalize)
  }

  def histogramBinsWithBounds(seq: Seq[Double],
                              bounds: Bounds,
                              numBins: Int = 20,
                              normalize: Boolean = false): Seq[ContinuousBin] = {
    val xbounds = bounds
    val binWidth = xbounds.range / numBins
    val grouped = seq.groupBy { value =>
      math.min(((value - xbounds.min) / binWidth).toInt, numBins - 1)
    }
    (0 until numBins).map { i =>
      val x = i * binWidth + xbounds.min
      grouped.get(i).map { vs =>
        ContinuousBin(vs, Bounds(x, x + binWidth))
      }.getOrElse(ContinuousBin(Seq.empty[Double], Bounds(x, x + binWidth)))
    }
  }
}

case class BinArgs[T](data: Seq[T], ctx: Option[PlotContext]) {

  def histogramBins(extract: T => Double,
                    numBins: Int = 20,
                    normalize: Boolean = false): Seq[ContinuousBin] = {
    Binning.histogramBins(data.map(extract), numBins, normalize)
  }

  def histogramBinsWithPlotBounds(extract: T => Double,
                                  ctx: Option[PlotContext],
                                  numBins: Int = 20,
                                  normalize: Boolean = false): Seq[ContinuousBin] = {
    Binning.histogramBinsWithPlotBounds(data.map(extract), ctx, numBins, normalize)
  }

  def histogramBinsWithBounds(extract: T => Double,
                              bounds: Bounds,
                              numBins: Int = 20,
                              normalize: Boolean = false): Seq[ContinuousBin] = {
    Binning.histogramBinsWithBounds(data.map(extract), bounds, numBins, normalize)
  }
}

object BinnedPlot {

  type ContextToDrawableContinuous[T] = ContinuousDataRenderer[T] => PlotContext => PlotRenderer

  def continuous[T](data: Seq[T],
                    binFn: BinArgs[T] => Seq[ContinuousBin],
                    xboundBuffer: Option[Double] = None,
                    yboundBuffer: Option[Double] = None
              )(
                contextToDrawable: ContextToDrawableContinuous[T]*,
              )(implicit theme: Theme): Plot = {
    val bins: Seq[ContinuousBin] = binFn(BinArgs(data, None))

    val xbounds = Bounds.union(bins.map(_.x))
    val ybounds = Bounds(0, bins.map(_.y).max)

    val groupedDataRenderer = ContinuousDataRenderer[T](data, binFn)

    Plot(
      xbounds,
      ybounds,
      CompoundPlotRenderer(
        contextToDrawable.map(x => x(groupedDataRenderer)),
        xbounds,
        ybounds
      )
    )
  }
}

case class ContinuousDataRenderer[T](data: Seq[T], binFn: BinArgs[T] => Seq[ContinuousBin]) {

  def manipulate(x: Seq[T] => Seq[T]): Seq[T] = x(data)

  def filter(x: T => Boolean): ContinuousDataRenderer[T] = this.copy(data.filter(x))

  def histogram(barRenderer: Option[BarRenderer] = None,
                spacing: Option[Double] = None,
                boundBuffer: Option[Double] = None,
               )(pCtx: PlotContext)(implicit theme: Theme) = {
    val bins = binFn(BinArgs(data, Some(pCtx)))
    Histogram.fromBins(bins, barRenderer, spacing, boundBuffer).renderer
  }

}
