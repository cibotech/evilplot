package com.cibo.evilplot.plot

import com.cibo.evilplot.colors.Color
import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent, LineStyle}
import com.cibo.evilplot.numeric.{Bounds, BoxPlotSummaryStatistics, Datum2d, Point}
import com.cibo.evilplot.plot.Histogram.{HistogramRenderer, createBins, defaultBinCount}
import com.cibo.evilplot.plot.LinePlot.LinePlotRenderer
import com.cibo.evilplot.plot.ScatterPlot.ScatterPlotRenderer
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.renderers.BoxRenderer.BoxRendererContext
import com.cibo.evilplot.plot.renderers._

object CartesianPlot {

  import TransformWorldToScreen._
  final case class CartesianPlotRenderer(drawablesToPlot: Seq[RenderContext => PlotRenderer],
                                         xBounds: Bounds,
                                         yBounds: Bounds) extends PlotRenderer {
    override def legendContext: LegendContext = LegendContext()


    def render(plot: Plot, plotExtent: Extent)(implicit theme: Theme): Drawable = {
      drawablesToPlot.foldLeft(EmptyDrawable() : Drawable){ case (accum, dr) =>
        dr(RenderContext(plot, plotExtent)).render(plot, plotExtent) behind accum
      }
    }
  }

  type ContextToDrawable[X <: Datum2d[X]] = CartesianDataRenderer[X] => RenderContext => PlotRenderer

  def apply[X <: Datum2d[X]](
                              data: Seq[X],
                              xboundBuffer: Option[Double] = None,
                              yboundBuffer: Option[Double] = None
                            )(
                              contextToDrawable: ContextToDrawable[X]*,
                            )(implicit theme: Theme): Plot = {

    val (xbounds, ybounds) = PlotUtils.bounds(data, theme.elements.boundBuffer, xboundBuffer, yboundBuffer)

    val cartesianDataRenderer = CartesianDataRenderer(data)

    Plot(
      xbounds,
      ybounds,
      CartesianPlotRenderer(
        contextToDrawable.map(x => x(cartesianDataRenderer)),
        xbounds,
        ybounds
      )
    )
  }
}


case class CartesianDataRenderer[X <: Datum2d[X]](data: Seq[X]) extends TransformWorldToScreen {

  def transformWTS(plotContext: RenderContext): Seq[X] = {
    val transformed = transformDatumToPlotSpace(
      data,
      plotContext.xCartesianTransform,
      plotContext.yCartesianTransform)
    transformed
  }

  def scatter(pointToDrawable: X => Drawable,
              legendCtx: LegendContext = LegendContext.empty)(pCtx: RenderContext)(implicit theme: Theme): PlotRenderer = {
    ScatterPlotRenderer(data, new PointRenderer[X] {
      def render(index: X): Drawable = pointToDrawable(index)
    })
  }

  def scatter(pointRenderer: PointRenderer[X])(pCtx: RenderContext)(implicit theme: Theme): ScatterPlotRenderer[X] = {
    ScatterPlotRenderer(data, pointRenderer)
  }


  def histogram(dataFn: X => Double,
                bins: Int = defaultBinCount,
                barRenderer: Option[BarRenderer] = None,
                spacing: Option[Double] = None,
                boundBuffer: Option[Double] = None,
                binningFunction: (Seq[Double], Bounds, Int) => Seq[Point] = createBins
               )(pCtx: RenderContext)(implicit theme: Theme) = {

    Histogram(data.map(dataFn), bins, barRenderer, spacing, boundBuffer, binningFunction).renderer
  }

  def histogram(dataFn: X => Double,
                barRenderer: BarRenderer,
                binCount: Int,
                spacing: Double,
                boundBuffer: Double,
                binningFunction: (Seq[Double], Bounds, Int) => Seq[Point]
               )(pCtx: RenderContext)(implicit theme: Theme) = {

    HistogramRenderer(data.map(dataFn), barRenderer = barRenderer, binCount, spacing, boundBuffer, binningFunction)
  }

  def manipulate(x: Seq[X] => Seq[X]): Seq[X] = x(data)

  def filter(x: X => Boolean): CartesianDataRenderer[X] = this.copy(data.filter(x))

  def line(
            strokeWidth: Option[Double] = None,
            color: Option[Color] = None,
            label: Drawable = EmptyDrawable(),
            lineStyle: Option[LineStyle] = None,
            legendCtx: LegendContext = LegendContext.empty
          )(pCtx: RenderContext)(implicit theme: Theme): PlotRenderer = {
    LinePlotRenderer(data, PathRenderer.default(strokeWidth, color, label, lineStyle))
  }

  def line(pathRenderer: PathRenderer[X])(pCtx: RenderContext)(implicit theme: Theme): PlotRenderer = {
    LinePlotRenderer(data, pathRenderer)
  }

  def boxAndWhisker(
                     dataGroupFn: Seq[X] => Seq[Seq[Double]],
                     quantiles: (Double, Double, Double) = (0.25, 0.50, 0.75),
                     spacing: Option[Double] = None,
                     boundBuffer: Option[Double] = None,
                     boxRenderer: Option[BoxRenderer] = None,
                     pointRenderer: Option[PointRenderer[BoxPlotPoint]] = None
                   )(pCtx: RenderContext)(implicit theme: Theme): Drawable = {
    val groupedData = dataGroupFn(data)
    val boxContexts = dataGroupFn(data).zipWithIndex.map {
      case (dist, index) =>
        if (dist.nonEmpty) {
          val summary = BoxPlotSummaryStatistics(dist, quantiles)
          Some(BoxRendererContext(summary, index))
        } else None
    }
    makePlot(
      dataGroupFn(data),
      boxContexts,
      spacing,
      None,
      boundBuffer,
      boxRenderer,
      pointRenderer
    ).render(pCtx.extent)
  }

  private def makePlot(
                        data: Seq[Seq[Double]],
                        boxContexts: Seq[Option[BoxRendererContext]],
                        spacing: Option[Double] = None,
                        clusterSpacing: Option[Double] = None,
                        boundBuffer: Option[Double] = None,
                        boxRenderer: Option[BoxRenderer] = None,
                        pointRenderer: Option[PointRenderer[BoxPlotPoint]] = None
                      )(implicit theme: Theme): Plot = {
    val xbounds = Bounds(0, boxContexts.size)
    val ybounds = Plot.expandBounds(
      Bounds(
        data.flatten.reduceOption[Double](math.min).getOrElse(0),
        data.flatten.reduceOption[Double](math.max).getOrElse(0)
      ),
      boundBuffer.getOrElse(theme.elements.boundBuffer)
    )
    Plot(
      xbounds,
      ybounds,
      BoxPlotRenderer(
        boxContexts,
        boxRenderer.getOrElse(BoxRenderer.default()),
        pointRenderer.getOrElse(PointRenderer.default[BoxPlotPoint]()),
        spacing.getOrElse(theme.elements.boxSpacing),
        clusterSpacing
      )
    )
  }

}