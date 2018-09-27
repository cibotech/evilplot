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

import com.cibo.evilplot.geometry._
import com.cibo.evilplot.plot.{LegendContext, LegendStyle}

/** Renderer to convert data and a legend context into a drawable. */
trait LegendRenderer {
  def render(context: LegendContext): Drawable
}

object LegendRenderer {

  val leftPadding: Double = 4
  val spacing: Double = 4

  def custom(fn: LegendContext => Drawable): LegendRenderer = new LegendRenderer {
    def render(context: LegendContext): Drawable = fn(context)
  }

  /** Create a legend for discrete components.
    * @param reduction Function to combine multiple legends.
    */
  def discrete(
    reduction: (Drawable, Drawable) => Drawable = above
  ): LegendRenderer = new LegendRenderer {
    def render(context: LegendContext): Drawable = {
      val labels = context.labels
      val elementSize = labels.maxBy(_.extent.height).extent.height
      val elementExtent = Extent(elementSize, elementSize)
      context.elements
        .zip(labels)
        .map {
          case (element, label) =>
            Align
              .middle(element, label.padLeft(leftPadding))
              .reduce(_ beside _)
              .padAll(spacing / 2)
        }
        .reduce(reduction)
    }
  }

  /** Create a legend with a gradient for continuous components.
    * @param reduction Function to combine multiple legends.
    */
  def gradient(
    reduction: (Drawable, Drawable) => Drawable = above
  ): LegendRenderer = new LegendRenderer {
    def render(context: LegendContext): Drawable = {
      val (startLabel, stopLabel) = (context.labels.head, context.labels.last)
      val elementSize = math.max(startLabel.extent.height, stopLabel.extent.height)
      val elementExtent = Extent(elementSize, elementSize)
      val inner = context.elements
        .map { element =>
          val offsetx = (elementSize - element.extent.width) / 2
          val offsety = (elementSize - element.extent.height) / 2
          element.translate(x = offsetx, y = offsety).resize(elementExtent)
        }
        .reduce(reduction)
      Seq(startLabel.padAll(spacing / 2), inner, stopLabel.padAll(spacing / 2)).reduce(reduction)
    }
  }

  def continuousGradient(
    reduction: (Drawable, Drawable) => Drawable = above
  ): LegendRenderer = new LegendRenderer {

    def render(context: LegendContext): Drawable = {
      if (context.gradientLegends.length > 1) {
        context.gradientLegends.reduce(reduction)
      } else context.gradientLegends.headOption.getOrElse(EmptyDrawable())
    }
  }

  /** Create a legend using the default style
    * @param reduction Function to combine multiple legends.
    */
  def default(
    reduction: (Drawable, Drawable) => Drawable = above
  ): LegendRenderer = new LegendRenderer {
    def render(context: LegendContext): Drawable = {
      context.defaultStyle match {
        case LegendStyle.Categorical => discrete(reduction).render(context).padLeft(1)
        case LegendStyle.Gradient    => gradient(reduction).render(context).padLeft(1)
        case LegendStyle.ContinuousGradient =>
          continuousGradient(reduction).render(context).padLeft(1)
      }
    }
  }

  def vertical(): LegendRenderer = default(above)
  def horizontal(): LegendRenderer = default(beside)

  def verticalDiscrete(): LegendRenderer = discrete(above)
  def horizontalDiscrete(): LegendRenderer = discrete(beside)

  def verticalGradient(): LegendRenderer = gradient(above)
  def horizontalGradient(): LegendRenderer = gradient(beside)
}
