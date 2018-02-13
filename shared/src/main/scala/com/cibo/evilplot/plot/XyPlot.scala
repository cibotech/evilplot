package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry._
import com.cibo.evilplot.numeric.{Bounds, Point}
import com.cibo.evilplot.plot.renderers.{PathRenderer, PlotRenderer, PointRenderer}

object XyPlot {

  val defaultBoundBuffer: Double = 0.1

  private case class XyPlotRenderer(
    data: Seq[Point],
    pointRenderer: PointRenderer,
    pathRenderer: PathRenderer
  ) extends PlotRenderer {
    def render(plot: Plot, plotExtent: Extent): Drawable = {
      val xtransformer = plot.xtransform(plot, plotExtent)
      val ytransformer = plot.ytransform(plot, plotExtent)
      val xformedPoints = data.filter(plot.inBounds).zipWithIndex.map { case (point, index) =>
        val x = xtransformer(point.x)
        val y = ytransformer(point.y)
        Point(x, y)
      }
      val points = xformedPoints.zipWithIndex.map { case (point, index) =>
        pointRenderer.render(plot, plotExtent, index).translate(x = point.x, y = point.y)
      }.group
      pathRenderer.render(plot, plotExtent, xformedPoints) inFrontOf points
    }
  }

  /** Create an XY plot (ScatterPlot, LinePlot are both special cases) from some data.
    *
    * @param data          The points to plot.
    * @param pointRenderer A function to create a Drawable for each point to plot.
    * @param pathRenderer A function to create a Drawable for all the points (such as a path)
    * @param boundBuffer   Extra padding to add to bounds as a fraction.
    * @return A Plot representing an XY plot.
    */
  def apply(
             data: Seq[Point],
             pointRenderer: PointRenderer = PointRenderer.default(),
             pathRenderer: PathRenderer = PathRenderer.default(),
             boundBuffer: Double = defaultBoundBuffer
           ): Plot = {
    require(boundBuffer >= 0.0)
    val xbounds = Plot.expandBounds(Bounds(data.minBy(_.x).x, data.maxBy(_.x).x), boundBuffer)
    val ybounds = Plot.expandBounds(Bounds(data.minBy(_.y).y, data.maxBy(_.y).y), boundBuffer)
    val legends = pointRenderer.legendContext.toSeq ++ pathRenderer.legendContext.toSeq
    Plot(xbounds, ybounds, XyPlotRenderer(data, pointRenderer, pathRenderer), legendContext = legends)
  }
}



