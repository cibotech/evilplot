package com.cibo.evilplot.plot.renderers

import com.cibo.evilplot.colors._
import com.cibo.evilplot.geometry.{Disc, Drawable, EmptyDrawable, Extent, Text}
import com.cibo.evilplot.numeric.Point
import com.cibo.evilplot.plot.{LegendContext, LegendStyle, Plot}

trait PointRenderer extends PlotElementRenderer[Int] {
  def render(plot: Plot, extent: Extent, index: Int): Drawable
}

object PointRenderer {

  val defaultPointSize: Double = 2.5
  val defaultColorCount: Int = 10

  /** Default function to create a label from a depth value. */
  def defaultLabelFunction(depth: Double): Drawable = {
    Text(math.ceil(depth).toString)
  }

  /** The default point renderer to render a disc.
    * @param size  The size of the point.
    * @param color The color of the point.
    * @param name An optional name to be shown in a legend.
    */
  def default(
    size: Double = defaultPointSize,
    color: Color = DefaultColors.barColor,
    name: Option[String] = None
  ): PointRenderer = new PointRenderer {
    override def legendContext: Option[LegendContext] = name.map { n =>
      LegendContext.single(Disc(size) filled color, n)
    }
    def render(plot: Plot, extent: Extent, index: Int): Drawable = Disc(size) filled color
  }

  /**
    * A no-op renderer for when you don't want to render points (such as on a line)
    */
  def empty(): PointRenderer = new PointRenderer {
    def render(plot: Plot, extent: Extent, index: Int): Drawable = EmptyDrawable()
  }

  /** Render points with colors based on depth.
    * @param depths The depths.
    * @param colorCount The number of labels/colors to use.
    * @param labelFunction Function to generate a label from a depth.
    * @param size The size of the point.
    */
  def depthColor(
    depths: Seq[Double],
    colorCount: Int = defaultColorCount,
    labelFunction: Double => Drawable = defaultLabelFunction,
    size: Double = defaultPointSize
  ): PointRenderer = {
    val bar = ScaledColorBar(Color.stream.take(colorCount), depths.min, depths.max)
    val labels = (0 until colorCount).map { c => labelFunction(bar.colorValue(c)) }
    depthColor(depths, labels, bar, size)
  }

  /** Render points with colors based on depth.
    * @param depths The depths.
    * @param labels Label for each category
    * @param bar The color bar to use
    * @param size The size of the point.
    */
  def depthColor(
    depths: Seq[Double],
    labels: Seq[Drawable],
    bar: ScaledColorBar,
    size: Double
  ): PointRenderer = {
    require(labels.lengthCompare(bar.nColors) == 0, "Number of labels does not match the number of categories")
    new PointRenderer {
      override def legendContext: Option[LegendContext] = {
        Some(
          LegendContext(
            elements = (0 until bar.nColors).map { c => Disc(size, size, size).filled(bar.getColor(c)) } ,
            labels = labels,
            defaultStyle = LegendStyle.Categorical
          )
        )
      }
      def render(plot: Plot, extent: Extent, index: Int): Drawable = {
        Disc(size) filled bar.getColor(depths(index))
      }
    }
  }

  /** Render points with colors based on depth.
    * @param depths The depths.
    * @param labels The labels to use for categories.
    * @param bar The color bar to use
    */
  def depthColor(
    depths: Seq[Double],
    labels: Seq[Drawable],
    bar: ScaledColorBar
  ): PointRenderer = depthColor(depths, labels, bar, defaultPointSize)

  /** Render points with colors based on depth.
    * @param depths The depths.
    * @param bar The color bar to use
    */
  def depthColor(
    depths: Seq[Double],
    bar: ScaledColorBar
  ): PointRenderer = {
    val labels = (0 until bar.nColors).map(c => defaultLabelFunction(bar.colorValue(c)))
    depthColor(depths, labels, bar, defaultPointSize)
  }
}
