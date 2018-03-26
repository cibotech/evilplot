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

import com.cibo.evilplot.colors.{Color, HSL, HTMLNamedColors}

object DefaultTheme {

  case class DefaultFonts(
    titleSize: Double = 22,
    labelSize: Double = 20,
    annotationSize: Double = 10,
    tickLabelSize: Double = 10,
    legendLabelSize: Double = 10,
    facetLabelSize: Double = 10
  ) extends Fonts

  case class DefaultColors(
    background: Color = HSL(0, 0, 92),
    bar: Color = HSL(0, 0, 35),
    fill: Color = HTMLNamedColors.white,
    path: Color = HSL(0, 0, 0),
    point: Color = HSL(0, 0, 35),
    gridLine: Color = HTMLNamedColors.white,
    trendLine: Color = HSL(0, 0, 35),
    title: Color = HTMLNamedColors.black,
    label: Color = HTMLNamedColors.black,
    annotation: Color = HTMLNamedColors.black,
    legendLabel: Color = HTMLNamedColors.black,
    tickLabel: Color = HTMLNamedColors.black,
    stream: Seq[Color] = Color.stream
  ) extends Colors

  case class DefaultElements(
    strokeWidth: Double = 2,
    pointSize: Double = 2.5,
    gridLineSize: Double = 1,
    barSpacing: Double = 1,
    clusterSpacing: Double = 4,
    boundBuffer: Double = 0.1,
    boxSpacing: Double = 20,
    contours: Int = 20,
    categoricalXAxisLabelOrientation: Double = 90,
    continuousXAxisLabelOrientation: Double = 0,
    xTickCount: Int = 10,
    yTickCount: Int = 10,
    xGridLineCount: Int = 10,
    yGridLineCount: Int = 10
  ) extends Elements

  case class DefaultTheme(
    fonts: Fonts = DefaultFonts(),
    colors: Colors = DefaultColors(),
    elements: Elements = DefaultElements()
  ) extends Theme

  implicit val defaultTheme: Theme = DefaultTheme()

}
