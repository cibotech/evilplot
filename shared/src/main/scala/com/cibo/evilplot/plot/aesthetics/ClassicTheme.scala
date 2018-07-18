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

import com.cibo.evilplot.colors.{Color, HSL, HTMLNamedColors, RGB}
import com.cibo.evilplot.plot.aesthetics.DefaultTheme.{DefaultColors, DefaultElements, DefaultFonts}

object ClassicTheme {
  val ClassicColors: Colors = DefaultColors.copy(
    background = HSL(0, 0, 92),
    frame = RGB(30, 30, 30),
    bar = HSL(0, 0, 35),
    fill = HTMLNamedColors.white,
    path = HSL(0, 0, 0),
    point = HSL(0, 0, 35),
    gridLine = HTMLNamedColors.white,
    trendLine = HSL(0, 0, 35),
    title = HTMLNamedColors.black,
    label = HTMLNamedColors.black,
    annotation = HTMLNamedColors.black,
    legendLabel = HTMLNamedColors.black,
    tickLabel = HTMLNamedColors.black,
    stream = Color.stream
  )

  val ClassicElements: Elements = DefaultElements.copy(
    pointSize = 2.5,
    gridLineSize = 1,
    categoricalXAxisLabelOrientation = 90
  )

  val ClassicFonts: Fonts = DefaultFonts.copy(
    tickLabelSize = 10,
    legendLabelSize = 10
  )

  implicit val classicTheme: Theme = Theme(
    colors = ClassicColors,
    elements = ClassicElements,
    fonts = ClassicFonts
  )
}
