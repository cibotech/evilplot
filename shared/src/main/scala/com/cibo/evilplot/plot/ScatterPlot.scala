package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.{Drawable, Extent}
import com.cibo.evilplot.numeric.{Bounds, Point}
import com.cibo.evilplot.plot.renderers.PointRenderer

object ScatterPlot {

  val defaultBoundBuffer: Double = 0.1

  private def renderScatter(pointRenderer: PointRenderer)(plot: Plot[Seq[Point]], plotExtent: Extent): Drawable = {
    val xtransformer = plot.xtransform(plot, plotExtent)
    val ytransformer = plot.ytransform(plot, plotExtent)
    plot.data.filter(plot.inBounds).zipWithIndex.map { case (point, index) =>
      val x = xtransformer(point.x)
      val y = ytransformer(point.y)
      pointRenderer.render(index).translate(x = x, y = y)
    }.group
  }

  /** Create a scatter plot from some data.
    * @param data The points to plot.
    * @param pointRenderer A function to create a Drawable for each point to plot.
    * @param boundBuffer Extra padding to add to bounds as a fraction.
    * @return A Plot representing a scatter plot.
    */
  def apply(
    data: Seq[Point],
    pointRenderer: PointRenderer = PointRenderer.default(),
    boundBuffer: Double = defaultBoundBuffer
  ): Plot[Seq[Point]] = {
    require(boundBuffer >= 0.0)
    val xbounds = Plot.expandBounds(Bounds(data.minBy(_.x).x, data.maxBy(_.x).x), boundBuffer)
    val ybounds = Plot.expandBounds(Bounds(data.minBy(_.y).y, data.maxBy(_.y).y), boundBuffer)
    Plot[Seq[Point]](data, xbounds, ybounds, renderScatter(pointRenderer))
  }
}
