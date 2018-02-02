package com.cibo.evilplot.plot2d

import com.cibo.evilplot.colors._
import com.cibo.evilplot.geometry.{Disc, Drawable}

trait PointRenderer {
  def render(index: Int): Drawable
}

object PointRenderer {

  val defaultPointSize: Double = 2.5
  val defaultColorCount: Int = 10

  /** The default point renderer to render a disc.
    * @param size  The size of the point.
    * @param color The color of the point.
    */
  def default(
    size: Double = defaultPointSize,
    color: Color = DefaultColors.barColor
  ): PointRenderer = new PointRenderer {
    def render(index: Int): Drawable = Disc(size) filled color
  }

  /** Render points with colors based on depth.
    * @param depths The depths.
    * @param colorCount The number of colors to use.
    * @param size The size of the point.
    */
  def depthColor(
    depths: Seq[Double],
    colorCount: Int = defaultColorCount,
    size: Double = defaultPointSize
  ): PointRenderer = {
    val bar = ScaledColorBar(Color.stream.take(colorCount), depths.min, depths.max)
    depthColor(depths, bar, size)
  }

  /** Render points with colors based on depth.
    * @param depths The depths.
    * @param bar The color bar to use
    * @param size The size of the point.
    */
  def depthColor(
    depths: Seq[Double],
    bar: ScaledColorBar,
    size: Double
  ): PointRenderer = new PointRenderer {
    def render(index: Int): Drawable = Disc(size) filled bar.getColor(depths(index))
  }

  /** Render points with colors based on depth.
    * @param depths The depths.
    * @param bar The color bar to use
    */
  def depthColor(
    depths: Seq[Double],
    bar: ScaledColorBar
  ): PointRenderer = depthColor(depths, bar, defaultPointSize)
}
