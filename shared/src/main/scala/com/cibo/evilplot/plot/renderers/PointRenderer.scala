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
import com.cibo.evilplot.numeric.{Datum2d, Point}
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.{Bar, LegendContext, LegendStyle, Plot, PlotContext}

trait PointRenderer[T <: Datum2d[T]] extends PlotElementRenderer[T] {
  def legendContext: LegendContext = LegendContext()

  /** Render a category within the extent. */
  def render(plot: Plot, extent: Extent, context: T): Drawable = render(context)

  def render(extent: Extent, context: T): Drawable = render(context)

  def render(datum: T): Drawable

}
object PointRenderer {

  val defaultColorCount: Int = 10

  def custom[T <: Datum2d[T]](
    renderFn: T => Drawable,
    legendCtx: Option[LegendContext] = None): PointRenderer[T] = new PointRenderer[T] {

    def render(datum: T): Drawable = {
      renderFn(datum)
    }

    override def legendContext: LegendContext = legendCtx.getOrElse(super.legendContext)
  }

  /** The default point renderer to render a disc.
    * @param color The color of the point.
    * @param pointSize The size of the point.
    * @param label Label to be shown in a legend.
    */
  def default[T <: Datum2d[T]](
    color: Option[Color] = None,
    pointSize: Option[Double] = None,
    label: Drawable = EmptyDrawable()
  )(implicit theme: Theme): PointRenderer[T] = new PointRenderer[T] {
    override def legendContext: LegendContext = label match {
      case _: EmptyDrawable => LegendContext.empty
      case d =>
        val size = pointSize.getOrElse(theme.elements.pointSize)
        LegendContext.single(Disc.centered(size).filled(color.getOrElse(theme.colors.point)), d)
    }
    def render(index: T): Drawable = {
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
  def depthColor[T <: Datum2d[T]](
    depth: T => Double,
    min: Double,
    max: Double,
    coloring: Option[Coloring[Double]] = None,
    size: Option[Double] = None
  )(implicit theme: Theme): PointRenderer[T] = new PointRenderer[T] {
    private val useColoring = coloring.getOrElse(theme.colors.continuousColoring)
    private val colorFunc = useColoring(Seq(min, max))
    private val radius = size.getOrElse(theme.elements.pointSize)

    def render(element: T): Drawable = {
      Disc.centered(radius).filled(colorFunc(depth(element)))
    }

    override def legendContext: LegendContext =
      useColoring.legendContext(Seq(min, max))
  }

  /**
    * Render points with colors based on a third, categorical variable.
    * @param colorDimension Categories for each point.
    * @param coloring The coloring to use. If not provided, one based on the
    *                 color stream from the theme is used.
    * @param size The size of the points.
    * @tparam T the type of the data
    * @tparam CATEGORY the type of the categorical variable.
    */
  def colorByCategory[T <: Datum2d[T], CATEGORY: Ordering](
    data: Seq[T],
    categoryExtract: T => CATEGORY,
    coloring: Option[Coloring[CATEGORY]] = None,
    size: Option[Double] = None)(implicit theme: Theme): PointRenderer[T] = new PointRenderer[T] {
    val categories = data.map(categoryExtract)
    private val useColoring = coloring.getOrElse(CategoricalColoring.themed[CATEGORY])
    private val colorFunc = useColoring(categories)
    private val radius = size.getOrElse(theme.elements.pointSize)

    def render(element: T): Drawable = {
      Disc.centered(radius).filled(colorFunc(categoryExtract(element)))
    }

    override def legendContext: LegendContext = useColoring.legendContext(categories)
  }

  /**
    * A no-op renderer for when you don't want to render points (such as on a line)
    */
  def empty[T <: Datum2d[T]](): PointRenderer[T] = new PointRenderer[T] {
    def render(index: T): Drawable = EmptyDrawable()
  }

  // Old `depthColor` implementation, called to by all deprecated `depthColor`
  // methods.
  private[this] def oldDepthColor[T <: Datum2d[T]](
    depth: T => Double,
    labels: Seq[Drawable],
    bar: ScaledColorBar,
    size: Option[Double]
  )(implicit theme: Theme): PointRenderer[T] = {
    require(
      labels.lengthCompare(bar.nColors) == 0,
      "Number of labels does not match the number of categories")
    val pointSize = size.getOrElse(theme.elements.pointSize)
    new PointRenderer[T] {
      override def legendContext: LegendContext = {
        LegendContext(
          elements = (0 until bar.nColors).map { c =>
            Disc(pointSize).filled(bar.getColor(c))
          },
          labels = labels,
          defaultStyle = LegendStyle.Categorical
        )
      }

      def render(index: T): Drawable = {
        Disc.centered(pointSize) filled bar.getColor(depth(index))
      }
    }
  }

}
