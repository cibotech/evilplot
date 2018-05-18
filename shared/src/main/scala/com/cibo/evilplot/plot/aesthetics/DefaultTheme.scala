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

package com.cibo.evilplot.plot.aesthetics

import com.cibo.evilplot.colors._
import com.cibo.evilplot.colors.ContinuousColoring.gradient
import com.cibo.evilplot.geometry.LineStyle

object DefaultTheme {
  private val darkGray: HSLA = HSLA(0, 0, 12, 1.0)
  private val lightGray: HSLA = HSLA(0, 0, 65, 0.8)
  private val darkBlue: HSLA = HSLA(211, 38, 48, 1.0)
  val colorPalette = Seq(
    HEX("#4c78a8"),
    HEX("#f58518"),
    HEX("#e45756"),
    HEX("#72b7b2"),
    HEX("#54a24b"),
    HEX("#eeca3b"),
    HEX("#b279a2"),
    HEX("#ff9da6"),
    HEX("#9d755d"),
    HEX("#bab0ac"))

  case class DefaultFonts(
    titleSize: Double = 22,
    labelSize: Double = 20,
    annotationSize: Double = 10,
    tickLabelSize: Double = 14,
    legendLabelSize: Double = 14,
    facetLabelSize: Double = 14,
    fontFace: String = "sans-serif"
  ) extends Fonts

  case class DefaultColors(
    background: Color = Clear,
    frame: Color = RGB(30, 30, 30),
    bar: Color = darkBlue,
    fill: Color = darkBlue,
    path: Color = darkGray,
    point: Color = darkGray,
    gridLine: Color = lightGray,
    trendLine: Color = darkGray,
    title: Color = darkGray,
    label: Color = darkGray,
    annotation: Color = darkGray,
    legendLabel: Color = darkGray,
    tickLabel: Color = darkGray,
    stream: Seq[Color] = colorPalette,
    continuousColoring: ContinuousColoring =
      gradient(HTMLNamedColors.blue, HTMLNamedColors.orange, gradientMode = GradientMode.Natural)
  ) extends Colors

  case class DefaultElements(
    strokeWidth: Double = 2,
    pointSize: Double = 4,
    gridLineSize: Double = 0.25,
    barSpacing: Double = 1,
    clusterSpacing: Double = 4,
    boundBuffer: Double = 0.0,
    boxSpacing: Double = 20,
    contours: Int = 20,
    categoricalXAxisLabelOrientation: Double = 0,
    continuousXAxisLabelOrientation: Double = 0,
    xTickCount: Int = 5,
    yTickCount: Int = 5,
    xGridLineCount: Int = 5,
    yGridLineCount: Int = 5,
    tickThickness: Double = 1,
    tickLength: Double = 5,
    lineDashStyle: LineStyle = LineStyle.Solid
  ) extends Elements

  case class DefaultTheme(
    fonts: Fonts = DefaultFonts(),
    colors: Colors = DefaultColors(),
    elements: Elements = DefaultElements()
  ) extends Theme

  implicit val defaultTheme: Theme = DefaultTheme()

}
