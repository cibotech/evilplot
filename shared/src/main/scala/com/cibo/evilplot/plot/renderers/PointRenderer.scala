/*
 * Copyright (c) 2018, CiBO Technologies, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.cibo.evilplot.plot.renderers

import com.cibo.evilplot.colors._
import com.cibo.evilplot.geometry.{Disc, Drawable, EmptyDrawable, Extent, Style, Text}
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.{LegendContext, LegendStyle, Plot}
import com.cibo.evilplot.plot.ExplicitImplicits

trait PointRenderer extends PlotElementRenderer[Int] {
  def legendContext: LegendContext = LegendContext()
  def render(plot: Plot, extent: Extent, index: Int): Drawable
}

object PointRenderer extends ExplicitImplicits{

  val defaultColorCount: Int = 10

  /** The default point renderer to render a disc.
    * @param color The color of the point.
    * @param pointSize The size of the point.
    * @param label Label to be shown in a legend.
    */
  def default(
    color: Option[Color] = None,
    pointSize: Option[Double] = None,
    label: Drawable = EmptyDrawable()
  )(implicit theme: Theme): PointRenderer = new PointRenderer {
    override def legendContext: LegendContext = label match {
      case _: EmptyDrawable => LegendContext.empty
      case d =>
        val size = pointSize.getOrElse(theme.elements.pointSize)
        LegendContext.single(Disc.centered(size).filled(color.getOrElse(theme.colors.point)), d)
    }
    def render(plot: Plot, extent: Extent, index: Int): Drawable = {
      val size = pointSize.getOrElse(theme.elements.pointSize)
      Disc.centered(size).filled(color.getOrElse(theme.colors.point))
    }
  }

  /**
    * Render points with colors based on a third, continuous variable.
    * @param depths The depths for each point.
    * @param coloring The coloring to use.
    * @param size The size of the point.
    */
  def depthColor(
    depths: Seq[Double],
    coloring: Option[Coloring[Double]] = None,
    size: Option[Double] = None
  )(implicit theme: Theme): PointRenderer = new PointRenderer {
    private val useColoring = coloring.getOrElse(theme.colors.continuousColoring)
    private val colorFunc = useColoring(depths)(theme)
    private val radius = size.getOrElse(theme.elements.pointSize)

    def render(plot: Plot, extent: Extent, index: Int): Drawable = {
      Disc.centered(radius).filled(colorFunc(depths(index)))
    }

    override def legendContext: LegendContext =
      useColoring.legendContext(depths)(theme)
  }

  /**
    * Render points with colors based on a third, categorical variable.
    * @param colorDimension Categories for each point.
    * @param coloring The coloring to use. If not provided, one based on the
    *                 color stream from the theme is used.
    * @param size The size of the points.
    * @tparam A the type of the categorical variable.
    */
  def colorByCategory[A: Ordering](
    colorDimension: Seq[A],
    coloring: Option[Coloring[A]] = None,
    size: Option[Double] = None
  )(implicit theme: Theme): PointRenderer = new PointRenderer {
    private val useColoring = coloring.getOrElse(CategoricalColoring.themed[A])
    private val colorFunc = useColoring(colorDimension)(theme)
    private val radius = size.getOrElse(theme.elements.pointSize)

    def render(plot: Plot, extent: Extent, index: Int): Drawable = {
      Disc.centered(radius).filled(colorFunc(colorDimension(index)))
    }

    override def legendContext: LegendContext = useColoring.legendContext(colorDimension)(theme)
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
    */
  @deprecated("Use an overload taking a Coloring[Double]", since = "29 March 2018")
  def depthColor(
    depths: Seq[Double],
    colorCount: Int
  )(implicit theme: Theme): PointRenderer = {
    val bar = ScaledColorBar(Color.stream.take(colorCount), depths.min, depths.max)
    val labels = (0 until colorCount).map { c =>
      Style(
        Text(
          math.ceil(bar.colorValue(c)).toString,
          theme.fonts.legendLabelSize,
          theme.fonts.fontFace),
        theme.colors.legendLabel)
    }
    oldDepthColor(depths, labels, bar, None)(theme)
  }

  /** Render points with colors based on depth.
    * @param depths The depths.
    * @param labels Label for each category
    * @param bar The color bar to use
    * @param size The size of the point.
    */
  @deprecated("Use an overload taking a Coloring[Double]", since = "29 March 2018")
  def depthColor(
    depths: Seq[Double],
    labels: Seq[Drawable],
    bar: ScaledColorBar,
    size: Option[Double]
  )(implicit theme: Theme): PointRenderer = {
    oldDepthColor(depths, labels, bar, size)(theme)
  }

  /** Render points with colors based on depth.
    * @param depths The depths.
    * @param labels The labels to use for categories.
    * @param bar The color bar to use
    */
  @deprecated("Use an overload taking a Coloring[Double]", since = "29 March 2018")
  def depthColor(
    depths: Seq[Double],
    labels: Seq[Drawable],
    bar: ScaledColorBar
  )(implicit theme: Theme): PointRenderer = oldDepthColor(depths, labels, bar, None)(theme)

  /** Render points with colors based on depth.
    * @param depths The depths.
    * @param bar The color bar to use
    */
  @deprecated("Use an overload taking a Coloring[Double]", since = "29 March 2018")
  def depthColor(
    depths: Seq[Double],
    bar: ScaledColorBar
  )(implicit theme: Theme): PointRenderer = {
    val labels = (0 until bar.nColors).map { c =>
      Style(
        Text(
          math.ceil(bar.colorValue(c)).toString,
          theme.fonts.legendLabelSize,
          theme.fonts.fontFace),
        theme.colors.legendLabel)
    }
    oldDepthColor(depths, labels, bar, None)(theme)
  }

  // Old `depthColor` implementation, called to by all deprecated `depthColor`
  // methods.
  private[this] def oldDepthColor(
    depths: Seq[Double],
    labels: Seq[Drawable],
    bar: ScaledColorBar,
    size: Option[Double]
  )(implicit theme: Theme): PointRenderer = {
    require(
      labels.lengthCompare(bar.nColors) == 0,
      "Number of labels does not match the number of categories")
    val pointSize = size.getOrElse(theme.elements.pointSize)
    new PointRenderer {
      override def legendContext: LegendContext = {
        LegendContext(
          elements = (0 until bar.nColors).map { c =>
            Disc(pointSize).filled(bar.getColor(c))
          },
          labels = labels,
          defaultStyle = LegendStyle.Categorical
        )
      }

      def render(plot: Plot, extent: Extent, index: Int): Drawable = {
        Disc.centered(pointSize) filled bar.getColor(depths(index))
      }
    }
  }

}
