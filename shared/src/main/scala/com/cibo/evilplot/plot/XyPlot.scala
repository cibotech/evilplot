package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry._
import com.cibo.evilplot.numeric.{Bounds, Point}
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.renderers.{PathRenderer, PlotRenderer, PointRenderer}

object XyPlot {

  private case class XyPlotRenderer(
    data: Seq[Point],
    pointRenderer: PointRenderer,
    pathRenderer: PathRenderer
  ) extends PlotRenderer {
    override def legendContext: LegendContext = pointRenderer.legendContext.combine(pathRenderer.legendContext)
    def render(plot: Plot, plotExtent: Extent)(implicit theme: Theme): Drawable = {
      val xtransformer = plot.xtransform(plot, plotExtent)
      val ytransformer = plot.ytransform(plot, plotExtent)
      val xformedPoints = data.filter(plot.inBounds).zipWithIndex.map { case (point, index) =>
        val x = xtransformer(point.x)
        val y = ytransformer(point.y)
        Point(x, y)
      }
      val points = xformedPoints.zipWithIndex.flatMap { case (point, index) =>
        val r = pointRenderer.render(plot, plotExtent, index)
        if (r.isEmpty) None else Some(r.translate(x = point.x, y = point.y))
      }.group
      pathRenderer.render(plot, plotExtent, xformedPoints) inFrontOf points
    }
  }

  /** Create an XY plot (ScatterPlot, LinePlot are both special cases) from some data.
    *
    * @param data           The points to plot.
    * @param pointRenderer  A function to create a Drawable for each point to plot.
    * @param pathRenderer   A function to create a Drawable for all the points (such as a path)
    * @param xboundBuffer   Extra padding to add to bounds as a fraction.
    * @param yboundBuffer   Extra padding to add to bounds as a fraction.
    * @return A Plot representing an XY plot.
    */
  def apply(
    data: Seq[Point],
    pointRenderer: Option[PointRenderer] = None,
    pathRenderer: Option[PathRenderer] = None,
    xboundBuffer: Option[Double] = None,
    yboundBuffer: Option[Double] = None
  )(implicit theme: Theme): Plot = {
    require(xboundBuffer.getOrElse(0.0) >= 0.0)
    require(yboundBuffer.getOrElse(0.0) >= 0.0)
    val xbounds = Plot.expandBounds(
      Bounds(data.minBy(_.x).x, data.maxBy(_.x).x),
      xboundBuffer.getOrElse(theme.elements.boundBuffer)
    )
    val ybounds = Plot.expandBounds(
      Bounds(data.minBy(_.y).y, data.maxBy(_.y).y),
      yboundBuffer.getOrElse(theme.elements.boundBuffer)
    )
    Plot(
      xbounds,
      ybounds,
      XyPlotRenderer(data, pointRenderer.getOrElse(PointRenderer.default()), pathRenderer.getOrElse(PathRenderer.default()))
    )
  }
}



