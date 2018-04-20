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

import com.cibo.evilplot.colors.{Color, HTMLNamedColors}
import com.cibo.evilplot.geometry.{Drawable, Extent, Rect, Text, Wedge}
import com.cibo.evilplot.numeric.Bounds
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.renderers.PlotRenderer

object PieChart {

  case class PieChartRenderer(
    data: Seq[(Drawable, Double)],
    colors: Seq[Color]
  ) extends PlotRenderer {

    override def legendContext: LegendContext = {
      val labelColors = data.map(_._1).zip(colors)
      LegendContext(
        elements = labelColors.map { lc =>
          Rect(Text.defaultSize, Text.defaultSize).filled(lc._2)
        },
        labels = labelColors.map(_._1),
        defaultStyle = LegendStyle.Categorical
      )
    }

    def render(plot: Plot, plotExtent: Extent)(implicit theme: Theme): Drawable = {

      val radius = math.min(plotExtent.width, plotExtent.height) / 2

      val total: Double = data.map(_._2).sum
      val totalRotations = data.tail.scanLeft(data.head._2 * 360 / total) {
        case (acc, (_, value)) =>
          acc + value * 360.0 / total
      } :+ 360.0

      val wedges = data.zip(totalRotations).zip(colors).map {
        case (((_, value), totalRotation), color) =>
          Wedge(totalRotation, radius).filled(color)
      }

      val labels = data.zip(totalRotations).map {
        case ((label, value), totalRotation) =>
          val radians = math.toRadians(totalRotation - value * 180 / total)
          val xoffset = math.cos(radians) * radius / 2
          val yoffset = math.sin(radians) * radius / 2
          label.translate(x = radius + xoffset, y = radius + yoffset)
      }

      wedges.reverse.group behind labels.group
    }
  }

  def apply(
    data: Seq[(String, Double)],
    colors: Seq[Color] = Seq.empty,
    textColor: Option[Color] = None,
    textSize: Option[Double] = None
  )(implicit theme: Theme): Plot = {
    val colorStream = if (colors.nonEmpty) colors else theme.colors.stream
    val withLabels = data.map { v =>
      Text(
        v._1,
        textSize.getOrElse(theme.fonts.legendLabelSize)
      ).filled(textColor.getOrElse(theme.colors.legendLabel)) -> v._2
    }
    Plot(
      xbounds = Bounds(0, 1),
      ybounds = Bounds(0, 1),
      renderer = PieChartRenderer(withLabels, colorStream)
    )
  }

}
