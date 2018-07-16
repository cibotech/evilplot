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

package com.cibo.evilplot.colors

import com.cibo.evilplot.geometry.{Disc, Drawable, Rect, Style, Text}
import com.cibo.evilplot.numeric.{AxisDescriptor, Bounds, Labeling}
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.{LegendContext, LegendStyle}

sealed trait Coloring[A] {
  def legendContext(dataToColor: Seq[A])(implicit theme: Theme): LegendContext
  def apply(dataToColor: Seq[A])(implicit theme: Theme): A => Color
}

trait CategoricalColoring[A] extends Coloring[A] {
  protected def distinctElemsAndColorFunction(dataToColor: Seq[A])(
    implicit theme: Theme): (Seq[A], A => Color)
  def apply(dataToColor: Seq[A])(implicit theme: Theme): A => Color = {
    distinctElemsAndColorFunction(dataToColor)._2
  }

  def legendContext(dataToColor: Seq[A])(implicit theme: Theme): LegendContext =
    legendContext(dataToColor, legendGlyph = (d: Double) => Disc(d))

  def legendContext(dataToColor: Seq[A], legendGlyph: Double => Drawable)(
    implicit theme: Theme): LegendContext = {
    val (distinct, coloring) = distinctElemsAndColorFunction(dataToColor)
    LegendContext(
      elements = distinct.map(v => legendGlyph(theme.elements.pointSize) filled coloring(v)),
      labels = distinct.map(
        a =>
          Style(
            Text(a.toString, theme.fonts.legendLabelSize, theme.fonts.fontFace),
            theme.colors.legendLabel)),
      defaultStyle = LegendStyle.Categorical
    )
  }
}
object CategoricalColoring {

  /**
    * Color a variable of type A using the default color stream for the plot's
    * theme.
    * This method will throw an exception if your plot's color stream does not
    * contain enough colors to satisfactorily color the data.
    **/
  def themed[A: Ordering]: CategoricalColoring[A] = new CategoricalColoring[A] {
    protected def distinctElemsAndColorFunction(dataToColor: Seq[A])(
      implicit theme: Theme): (Seq[A], A => Color) = {
      val distinctElems = dataToColor.distinct.sorted
      val colors = theme.colors.stream.take(distinctElems.length).toVector
      require(
        colors.length == distinctElems.length,
        s"The color stream for this plot theme does not have enough colors to color $distinctElems")
      (distinctElems, (a: A) => colors(distinctElems.indexOf(a)))
    }
  }

  /** Create a categorical coloring from a function.
    * @param enumerated a list of each label value, in the order it should appear
    *                   in the legend.
    * @param function how to color a value of type A.
    */
  def fromFunction[A](enumerated: Seq[A], function: A => Color): CategoricalColoring[A] =
    new CategoricalColoring[A] {
      protected def distinctElemsAndColorFunction(dataToColor: Seq[A])(
        implicit theme: Theme): (Seq[A], A => Color) = {
        (enumerated, function)
      }
    }

  /** Create a categorical coloring out of a gradient.
    * @param colors Colors to use as endpoints in the gradient.
    */
  def gradient[A: Ordering](
    colors: Seq[Color],
    gradientMode: GradientMode
  ): CategoricalColoring[A] =
    new CategoricalColoring[A] {
      require(colors.nonEmpty, "Cannot make a gradient out of zero colors.")
      protected def distinctElemsAndColorFunction(dataToColor: Seq[A])(
        implicit theme: Theme): (Seq[A], A => Color) = {
        val distinctElems: Seq[A] = dataToColor.distinct.sorted
        val f = GradientUtils.multiGradient(colors, 0, distinctElems.length - 1, gradientMode)
        (distinctElems, (a: A) => f(distinctElems.indexOf(a).toDouble))
      }
    }

  def gradient[A: Ordering](
    start: Color,
    end: Color,
    gradientMode: GradientMode = GradientMode.Linear
  ): CategoricalColoring[A] =
    gradient(Seq(start, end), gradientMode)
}

trait ContinuousColoring extends Coloring[Double] {
  protected def getDescriptor(
    min: Option[Double],
    max: Option[Double],
    dataToColor: Seq[Double],
    numDivisions: Int): AxisDescriptor = {
    val bounds = max
      .flatMap(mx => min.map(mn => Bounds(mn, mx)))
      .getOrElse(
        Bounds(
          dataToColor.reduceOption[Double](math.min).getOrElse(0.0),
          dataToColor.reduceOption[Double](math.max).getOrElse(0.0)))
    Labeling.label(
      bounds,
      preferredTickCount = Some(numDivisions),
      tickCountRange = Some(Seq(numDivisions)),
      fixed = min.isDefined || max.isDefined)
  }
}
object ContinuousColoring {

  /** Color using a 2-stop gradient.
    * @param start the left endpoint for interpolation
    * @param end the right endpoint for interpolation
    * @param min min override for the data
    * @param max max override for the data
    */
  def gradient(
    start: Color,
    end: Color,
    min: Option[Double] = None,
    max: Option[Double] = None,
    gradientMode: GradientMode = GradientMode.Linear
  ): ContinuousColoring = {
    gradient(Seq(start, end), min, max, gradientMode)
  }

  /** Color using 3 stop gradient.
    * @param start the left stop for interpolation
    * @param middle the middle stop for interpolation
    * @param end the right stop for interpolation
    * @param min min override for the data
    * @param max max override for the data
    */
  def gradient3(
    start: Color,
    middle: Color,
    end: Color,
    min: Option[Double] = None,
    max: Option[Double] = None,
    gradientMode: GradientMode = GradientMode.Linear): ContinuousColoring = {
    gradient(Seq(start, middle, end), min, max, gradientMode)
  }

  /** Color using a multi-stop gradient.
    * @param colors the colors to use as interpolation points
    * @param min min override for the data
    * @param max max override for the data
    */
  def gradient(
    colors: Seq[Color],
    min: Option[Double],
    max: Option[Double],
    gradientMode: GradientMode): ContinuousColoring =
    new ContinuousColoring {
      require(colors.nonEmpty, "Cannot make a gradient out of zero colors.")
      def apply(dataToColor: Seq[Double])(implicit theme: Theme): Double => Color = {
        val descriptor = getDescriptor(min, max, dataToColor, 5)
        GradientUtils.multiGradient(
          colors,
          descriptor.axisBounds.min,
          descriptor.axisBounds.max,
          gradientMode)
      }

      def legendContext(coloringDimension: Seq[Double])(implicit theme: Theme): LegendContext = {
        val descriptor = getDescriptor(min, max, coloringDimension, 5)
        val coloring = apply(coloringDimension)
        LegendContext(
          elements = descriptor.values.map(v =>
            Rect(theme.fonts.legendLabelSize, theme.fonts.legendLabelSize) filled coloring(v)),
          labels = descriptor.labels.map(
            l =>
              Style(
                Text(l, theme.fonts.legendLabelSize, theme.fonts.fontFace),
                theme.colors.legendLabel)),
          defaultStyle = LegendStyle.Gradient
        )
      }
    }

  /** Convenience coloring method when we know exactly what the values of the gradients are.
    * @param colors the colors to use as interpolation points
    * @param min min value
    * @param max max override for the data
    */
  def gradientColoringFunction(
    colors: Seq[Color],
    min: Double,
    max: Double,
    gradientMode: GradientMode)(implicit theme: Theme): Double => Color =
    gradient(colors, Some(min), Some(max), gradientMode)(Seq.empty)
}
