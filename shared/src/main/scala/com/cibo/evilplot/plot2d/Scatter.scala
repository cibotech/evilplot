package com.cibo.evilplot.plot2d

import com.cibo.evilplot.geometry.{Drawable, Extent, Translate}
import com.cibo.evilplot.numeric.{Bounds, Point}

object Scatter {

  private def renderScatter(pointRenderer: PointRenderer)(plot: Plot2D[Seq[Point]], extent: Extent): Drawable = {
    val xtransformer = plot.xtransform(plot, extent)
    val ytransformer = plot.ytransform(plot, extent)
    plot.data.filter(plot.inBounds).zipWithIndex.map { case (point, index) =>
      val x = xtransformer(point.x)
      val y = ytransformer(point.y)
      Translate(pointRenderer.render(index), x = x, y = y)
    }.group
  }

  /** Create a scatter plot from some data.
    * @param data The points to plot.
    * @param pointRenderer A function to create a Drawable for each point to plot.
    * @return A Plot2D representing a scatter plot.
    */
  def apply(
    data: Seq[Point],
    pointRenderer: PointRenderer = PointRenderer.default()
  ): Plot2D[Seq[Point]] = {
    val xbounds = Bounds(data.minBy(_.x).x, data.maxBy(_.x).x)
    val ybounds = Bounds(data.minBy(_.y).y, data.maxBy(_.y).y)
    Plot2D[Seq[Point]](data, xbounds, ybounds, renderScatter(pointRenderer)).background()
  }
}
