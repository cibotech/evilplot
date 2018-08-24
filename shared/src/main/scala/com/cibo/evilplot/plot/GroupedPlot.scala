package com.cibo.evilplot.plot

import com.cibo.evilplot.colors.Color
import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent, LineStyle}
import com.cibo.evilplot.numeric.{Bounds, Datum2d, Point}
import com.cibo.evilplot.plot
import com.cibo.evilplot.plot.Histogram.{HistogramRenderer, createBins, defaultBinCount}
import com.cibo.evilplot.plot.LinePlot.LinePlotRenderer
import com.cibo.evilplot.plot.ScatterPlot.ScatterPlotRenderer
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.renderers.{BarRenderer, PathRenderer, PlotRenderer, PointRenderer}

import scala.reflect.ClassTag

final case class CompoundPlotRenderer(drawablesToPlot: Seq[RenderContext => PlotRenderer],
                                      xBounds: Bounds,
                                      yBounds: Bounds) extends PlotRenderer {
  override def legendContext: LegendContext = LegendContext()

  def render(plot: Plot, plotExtent: Extent)(implicit theme: Theme): Drawable = {
    drawablesToPlot.foldLeft(EmptyDrawable() : Drawable){ case (accum, dr) =>
      dr(RenderContext(plot, plotExtent)).render(plot, plotExtent) behind accum
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

case class CategoryBin[T](values: Seq[Double], category: T, agg: Seq[Double] => Double = _.sum) extends Bin[Double]{
  lazy val x: T = category
  lazy val y: Double = agg(values)
}

object Binning {

  def histogramBinsFromContext(seq: Seq[Double], ctx: Option[RenderContext], numBins: Int = 20, normalize: Boolean = false): Seq[ContinuousBin] = {
    ctx match {
      case Some(rctx) => Binning.histogramBins(seq, rctx.xBounds)
      case None => Binning.histogramBinsDataBounds(seq)
    }
  }

  def histogramBinsDataBounds(seq: Seq[Double], numBins: Int = 20, normalize: Boolean = false): Seq[ContinuousBin] = {
    histogramBins(seq, Bounds(seq.min, seq.max), numBins, normalize)
  }

  def histogramBins(seq: Seq[Double], bounds: Bounds, numBins: Int = 20, normalize: Boolean = false): Seq[ContinuousBin] = {
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

//object CategoricalGroupedPlot {
//
//  type ContextToDrawable[T, A <: Bin[T], CAT] = ContinuousDataRenderer[T, A, CAT] => RenderContext => PlotRenderer
//
//  def continuous[T]( data: Seq[T],
//                     binFn: Seq[T] => Seq[ContinuousBin],
//                     xboundBuffer: Option[Double] = None,
//                     yboundBuffer: Option[Double] = None
//                   )(
//                     contextToDrawable: ContextToDrawable[T, ContinuousBin, Double]*,
//                   )(implicit theme: Theme): Plot = {
//    val bins: Seq[ContinuousBin] = binFn(data)
//
//    val xbounds = Bounds.union(bins.map(_.x))
//    val ybounds = Bounds.get(bins.map(_.y)).get
//
//    val groupedDataRenderer = ContinuousDataRenderer[T, ContinuousBin, Double](data, binFn)
//
//    Plot(
//      xbounds,
//      ybounds,
//      CompoundPlotRenderer(
//        contextToDrawable.map(x => x(groupedDataRenderer)),
//        xbounds,
//        ybounds
//      )
//    )
//  }
//
//  def categorical[T, CAT](
//                           data: Seq[T],
//                           binFn: Seq[T] => Seq[CategoryBin[CAT]],
//                           xboundBuffer: Option[Double] = None,
//                           yboundBuffer: Option[Double] = None
//                         )(
//                           contextToDrawable: ContextToDrawable[T, CategoryBin[CAT], CAT]*,
//                         )(implicit theme: Theme): Plot = {
//
//    val bins: Seq[CategoryBin[CAT]] = binFn(data)
//
//    val xbounds = Bounds(0, bins.length.toDouble)
//    val ybounds = Bounds.get(bins.map(_.y)).get
//
//    val groupedDataRenderer = ContinuousDataRenderer[T, CategoryBin[CAT], CAT](data, binFn)
//
//    Plot(
//      xbounds,
//      ybounds,
//      CompoundPlotRenderer(
//        contextToDrawable.map(x => x(groupedDataRenderer)),
//        xbounds,
//        ybounds
//      )
//    )
//  }
//}

object GroupedPlot {

  type ContextToDrawableContinuous[T] = ContinuousDataRenderer[T] => RenderContext => PlotRenderer

  def continuous[T]( data: Seq[T],
                binFn: (Seq[T], Option[RenderContext]) => Seq[ContinuousBin],
                xboundBuffer: Option[Double] = None,
                yboundBuffer: Option[Double] = None
              )(
                contextToDrawable: ContextToDrawableContinuous[T]*,
              )(implicit theme: Theme): Plot = {
    val bins: Seq[ContinuousBin] = binFn(data, None)

    val xbounds = Bounds.union(bins.map(_.x))
    val ybounds = Bounds(0, bins.map(_.y).max)

    val groupedDataRenderer = ContinuousDataRenderer[T](data, binFn)

    println("GROUPED PLOT XBOUNDS",xbounds)
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

  type ContextToDrawableCategorical[T, CAT] = CategoricalDataRenderer[T, CAT] => RenderContext => PlotRenderer

  def categorical[T, CAT]( data: Seq[T],
                      binFn: Seq[T] => Seq[CategoryBin[CAT]],
                      catLabel: CAT => String,
                      xboundBuffer: Option[Double] = None,
                      yboundBuffer: Option[Double] = None
                    )(
                      contextToDrawable: ContextToDrawableCategorical[T, CAT]*,
                    )(implicit theme: Theme): Plot = {
    val bins: Seq[CategoryBin[CAT]] = binFn(data)

    val xbounds = Bounds(0, bins.length)
    val ybounds = Bounds(0, bins.map(_.y).max)

    val groupedDataRenderer = plot.CategoricalDataRenderer[T, CAT](data, binFn)

    println("GROUPED PLOT XBOUNDS",xbounds)
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

case class CategoricalDataRenderer[T, CAT](data: Seq[T], binFn: Seq[T] => Seq[CategoryBin[CAT]]) {

  def manipulate(x: Seq[T] => Seq[T]): Seq[T] = x(data)

  def filter(x: T => Boolean): CategoricalDataRenderer[T, CAT] = this.copy(data.filter(x))

  def barChart(barRenderer: Option[BarRenderer] = None,
                spacing: Option[Double] = None,
                boundBuffer: Option[Double] = None,
               )(pCtx: RenderContext)(implicit theme: Theme): PlotRenderer = {
    BarChart(binFn(data).map(_.y)).renderer
  }

}

case class ContinuousDataRenderer[T](data: Seq[T], binFn: (Seq[T], Option[RenderContext]) => Seq[ContinuousBin]) {

  def manipulate(x: Seq[T] => Seq[T]): Seq[T] = x(data)

  def filter(x: T => Boolean): ContinuousDataRenderer[T] = this.copy(data.filter(x))

  def histogram(barRenderer: Option[BarRenderer] = None,
                spacing: Option[Double] = None,
                boundBuffer: Option[Double] = None,
               )(pCtx: RenderContext)(implicit theme: Theme) = {
    val bins = binFn(data, Some(pCtx))
    Histogram.continuous(bins, barRenderer, spacing, boundBuffer).renderer
  }

}
