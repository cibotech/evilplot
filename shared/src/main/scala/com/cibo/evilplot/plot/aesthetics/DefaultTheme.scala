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

  val DefaultFonts: Fonts = Fonts(
    titleSize = 22,
    labelSize = 20,
    annotationSize = 10,
    tickLabelSize = 14,
    legendLabelSize = 14,
    facetLabelSize = 14,
    fontFace = "sans-serif"
  )

  val DefaultColors: Colors = Colors(
    background = Clear,
    frame = RGB(30, 30, 30),
    bar = darkBlue,
    fill = darkBlue,
    path = darkGray,
    point = darkGray,
    gridLine = lightGray,
    trendLine = darkGray,
    title = darkGray,
    label = darkGray,
    annotation = darkGray,
    legendLabel = darkGray,
    tickLabel = darkGray,
    stream = colorPalette,
    continuousColoring =
      gradient(HTMLNamedColors.blue, HTMLNamedColors.orange, gradientMode = GradientMode.Natural)
  )

  val DefaultElements: Elements = Elements(
    strokeWidth = 2,
    pointSize = 4,
    gridLineSize = 0.25,
    barSpacing = 1,
    clusterSpacing = 4,
    boundBuffer = 0.0,
    boxSpacing = 20,
    contours = 20,
    categoricalXAxisLabelOrientation = 0,
    categoricalYAxisLabelOrientation = 0,
    continuousXAxisLabelOrientation = 0,
    continuousYAxisLabelOrientation = 0,
    tickCount = 5,
    xTickCount = 5,
    yTickCount = 5,
    xGridLineCount = 5,
    yGridLineCount = 5,
    tickThickness = 1,
    tickLength = 5,
    lineDashStyle = LineStyle.Solid
  )

  val DefaultTheme = Theme(
    fonts = DefaultFonts,
    colors = DefaultColors,
    elements = DefaultElements
  )

  implicit val defaultTheme: Theme = DefaultTheme

}
