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

package com.cibo.evilplot.plot

import com.cibo.evilplot.colors.{FillGradients, HTMLNamedColors, ScaledColorBar}
import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent, GradientFill, GradientStop, LinearGradient, Rect, Style, Text}
import com.cibo.evilplot.plot.aesthetics.Theme

sealed trait LegendStyle

object LegendStyle {
  case object Gradient extends LegendStyle // A legend of levels represented using a gradient.
  case object Categorical extends LegendStyle // A legend with distinct categories.
  case object ContinuousGradient extends LegendStyle
}

/** Context information used to render a legend for a plot.
  * @param elements The elements for each level.
  * @param labels Labels for each element.
  * @param defaultStyle The default legend style to render.
  */
case class LegendContext(
  elements: Seq[Drawable] = Seq.empty,
  labels: Seq[Drawable] = Seq.empty,
  defaultStyle: LegendStyle = LegendStyle.Categorical,
  gradientLegends: Seq[Drawable] = Seq.empty // move this eventually will break api if done now
 ) {
  require(elements.lengthCompare(labels.size) == 0, "Legend requires matching number of elements and labels")

  def isEmpty: Boolean = elements.isEmpty
  def nonEmpty: Boolean = !isEmpty

  // Combine this LegendContext with another, taking only new content.
  def combine(other: LegendContext): LegendContext = {
    val oldElementLabels = elements.zip(labels)
    val newElementLabels = other.elements.zip(other.labels).filterNot(oldElementLabels.contains)
    copy(
      elements = elements ++ newElementLabels.map(_._1),
      labels = labels ++ newElementLabels.map(_._2),
      defaultStyle = if (nonEmpty) defaultStyle else other.defaultStyle
    )
  }
}

object LegendContext {
  def empty: LegendContext = LegendContext()

  def single(
    element: Drawable,
    label: Drawable
  ): LegendContext = LegendContext(Seq(element), Seq(label), LegendStyle.Categorical)

  def single(
    element: Drawable,
    label: String
  )(implicit theme: Theme): LegendContext =
    single(
      element,
      Style(
        Text(label, size = theme.fonts.legendLabelSize, fontFace = theme.fonts.fontFace),
        theme.colors.legendLabel))

  def fromColorBar(
    colorBar: ScaledColorBar,
    style: LegendStyle = LegendStyle.Gradient
  )(implicit theme: Theme): LegendContext = {
    val elements = (0 until colorBar.nColors).map { c =>
      Rect(Text.defaultSize, Text.defaultSize).filled(colorBar.getColor(c))
    }
    val labels = (0 until colorBar.nColors).map { c =>
      val value = style match {
        case LegendStyle.Gradient if c == 0 =>
          // Floor if labeling the first value in a gradient.
          math.floor(colorBar.colorValue(c))
        case LegendStyle.Gradient =>
          // Ceiling if labeling the last value in a gradient.
          math.ceil(colorBar.colorValue(c))
        case LegendStyle.Categorical =>
          // Otherwise round
          math.round(colorBar.colorValue(c))
      }
      Style(
        Text(value.toString, size = theme.fonts.legendLabelSize, fontFace = theme.fonts.fontFace),
        theme.colors.legendLabel)
    }
    LegendContext(
      elements = elements,
      labels = labels,
      defaultStyle = LegendStyle.Gradient
    )
  }

  def continuousGradientFromColorBar(colorBar: ScaledColorBar
                                    )(implicit theme: Theme): LegendContext = {

    val stops = FillGradients.distributeEvenly(colorBar.colorSeq)

    val gradientBarSize = Extent(10, 100)
    val gradient = LinearGradient.topToBottom(gradientBarSize, stops)

    val minText = Text(colorBar.zMin.toString, theme.fonts.legendLabelSize).padAll(2).center(14)
    val maxText = Text(colorBar.zMax.toString, theme.fonts.legendLabelSize).padAll(2).center(14)

    val gradientLegend = (minText above Rect(gradientBarSize).filled(gradient).center(14) above maxText)

    LegendContext(
      elements = Seq(EmptyDrawable()), // this is crazy
      labels = Seq(EmptyDrawable()),
      gradientLegends = Seq(gradientLegend),
      defaultStyle = LegendStyle.ContinuousGradient
    )
  }

  def combine(contexts: Seq[LegendContext]): LegendContext = {
    contexts.foldLeft(LegendContext.empty) { (a, b) =>
      a.combine(b)
    }
  }
}
