package com.cibo.evilplot.plot

import com.cibo.evilplot.colors.Color
import com.cibo.evilplot.geometry.{Clipping, Drawable, EmptyDrawable, Extent, LineDash, LineStyle, Path, StrokeStyle}
import com.cibo.evilplot.numeric._
import com.cibo.evilplot.plot.BoxPlot.makePlot
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.renderers.BoxRenderer.BoxRendererContext
import com.cibo.evilplot.plot.renderers.{BoxRenderer, PathRenderer, PointRenderer}
import com.cibo.evilplot.plot.renderers.PathRenderer.calcLegendStrokeLength

trait TransformWorldToScreen {
  type Transformer = Double => Double

  def xCartesianTransformer(xBounds: Bounds, plotExtent: Extent): Double => Double = {
    val scale = plotExtent.width / xBounds.range
    (x: Double) => (x - xBounds.min) * scale
  }

  def yCartesianTransformer(yBounds: Bounds, plotExtent: Extent): Double => Double = {
    val scale = plotExtent.height / yBounds.range
    (y: Double) => { plotExtent.height - (y - yBounds.min) * scale}
  }

  def createTransformers(yBounds: Bounds, xBounds: Bounds, plotExtent: Extent): (Double => Double, Double => Double) = {
    val xtransformer = xCartesianTransformer(xBounds, plotExtent)
    val ytransformer = yCartesianTransformer(yBounds, plotExtent)

    (xtransformer, ytransformer)
  }

  def transformPointToWorld[X <: Datum2d[X]](point: X,
                                             xtransformer: Transformer,
                                             ytransformer: Transformer): X = {
    val x = xtransformer(point.x)
    val y = ytransformer(point.y)
    point.setXY(x = x,y = y)
  }

  def transformPointsToPlotSpace[X <: Datum2d[X]](data: Seq[X],
                                                  xtransformer: Transformer,
                                                  ytransformer: Transformer): Seq[X] = {

    data.map( p => transformPointToWorld(p, xtransformer, ytransformer))
  }
}

object TransformWorldToScreen extends TransformWorldToScreen

object PlotUtils extends TransformWorldToScreen {

  def bounds[X <: Datum2d[X]](data: Seq[X],
                              defaultBoundBuffer: Double,
                              xboundBuffer: Option[Double] = None,
                              yboundBuffer: Option[Double] = None): (Bounds, Bounds) = {
    require(xboundBuffer.getOrElse(0.0) >= 0.0)
    require(yboundBuffer.getOrElse(0.0) >= 0.0)
    val xs = data.map(_.x)
    val xbuffer = xboundBuffer.getOrElse(defaultBoundBuffer)
    val ybuffer = yboundBuffer.getOrElse(defaultBoundBuffer)
    val xbounds = Plot.expandBounds(
      Bounds(
        xs.reduceOption[Double](math.min).getOrElse(0.0),
        xs.reduceOption[Double](math.max).getOrElse(0.0)),
      if (data.length == 1 && xbuffer == 0) 0.1 else xbuffer
    )

    val ys = data.map(_.y)
    val ybounds = Plot.expandBounds(
      Bounds(
        ys.reduceOption[Double](math.min).getOrElse(0.0),
        ys.reduceOption[Double](math.max).getOrElse(0.0)),
      if (data.length == 1 && ybuffer == 0) 0.1 else xbuffer
    )
    (xbounds, ybounds)
  }

  case class PlotContext(xBounds: Bounds,
                          yBounds: Bounds,
                          plotExtent: Extent){
    def xCartesianTransform: Double => Double = TransformWorldToScreen.xCartesianTransformer(xBounds, plotExtent)
    def yCartesianTransform: Double => Double = TransformWorldToScreen.yCartesianTransformer(yBounds, plotExtent)
  }

  object PlotContext {
    def fromPlotExtent(plot: Plot, extent: Extent): PlotContext = apply(plot.xbounds, plot.ybounds, extent)
  }

  case class CartesianDataRenderer[X <: Datum2d[X]](data: Seq[X]){

    def transformWTS(plotContext: PlotContext): Seq[X] = {
      val (xtransformer, ytransformer) = createTransformers(plotContext.xBounds, plotContext.yBounds, plotContext.plotExtent)
      val transformed = transformPointsToPlotSpace(data, xtransformer, ytransformer)
      transformed
    }

    def scatter(pointToDrawable: X => Drawable)(pCtx: PlotContext): Drawable = {

      val points = transformWTS(pCtx).zipWithIndex
        .withFilter(p => pCtx.plotExtent.contains(p._1))
        .flatMap {
          case (point, index) =>
            val r = pointToDrawable(point)
            if (r.isEmpty) None else Some(r.translate(x = point.x, y = point.y))
        }
        .group

      points
    }

    def manipulate(x: Seq[X] => Seq[X]): Seq[X] = x(data)
    def filter(x: X => Boolean): CartesianDataRenderer[X] = this.copy(data.filter(x))

    def line(
            strokeWidth: Option[Double] = None,
            color: Option[Color] = None,
            label: Drawable = EmptyDrawable(),
            lineStyle: Option[LineStyle] = None
           )(pCtx: PlotContext)(implicit theme: Theme): Drawable = {

      Clipping
        .clipPath(transformWTS(pCtx), pCtx.plotExtent)
        .map(
          segment =>
            LineDash(
              StrokeStyle(
                Path(segment, strokeWidth.getOrElse(theme.elements.strokeWidth)),
                color.getOrElse(theme.colors.path)),
              lineStyle.getOrElse(theme.elements.lineDashStyle)
            )
        )
        .group
    }



    def boxAndWhisker(
      dataGroupFn: Seq[X] => Seq[Seq[Double]],
      quantiles: (Double, Double, Double) = (0.25, 0.50, 0.75),
      spacing: Option[Double] = None,
      boundBuffer: Option[Double] = None,
      boxRenderer: Option[BoxRenderer] = None,
      pointRenderer: Option[PointRenderer] = None
      )(pCtx: PlotContext)(implicit theme: Theme): Drawable = {
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
        ).render(pCtx.plotExtent)
      }

    private def makePlot(
                          data: Seq[Seq[Double]],
                          boxContexts: Seq[Option[BoxRendererContext]],
                          spacing: Option[Double] = None,
                          clusterSpacing: Option[Double] = None,
                          boundBuffer: Option[Double] = None,
                          boxRenderer: Option[BoxRenderer] = None,
                          pointRenderer: Option[PointRenderer] = None
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
          pointRenderer.getOrElse(PointRenderer.default()),
          spacing.getOrElse(theme.elements.boxSpacing),
          clusterSpacing
        )
      )
    }
//    def boxAndWhisker(spacing: Double,
//                      clusterSpacing: Option[Double],
//                      groupFn: Seq[X] => Seq[Seq[Double]],
//                      fillColor: Option[Color] = None,
//                      strokeColor: Option[Color] = None,
//                      lineDash: Option[LineDash] = None,
//                      strokeWidth: Option[Double] = None,
//                      quantiles: (Double, Double, Double) = (0.25, 0.50, 0.75)
//                     )(pCtx: PlotContext)(implicit theme: Theme) = {
//
//      val boxContexts = groupFn(data).zipWithIndex.map {
//        case (dist, index) =>
//          if (dist.nonEmpty) {
//            val summary = BoxPlotSummaryStatistics(dist, quantiles)
//            Some(BoxRendererContext(summary, index))
//          } else None
//      }
//      BoxRenderer.default(fillColor = fillColor, strokeColor = strokeColor, lineDash = lineDash, strokeWidth = strokeWidth).render(pCtx.plotExtent, boxContexts)
//
//    }

  }






}

