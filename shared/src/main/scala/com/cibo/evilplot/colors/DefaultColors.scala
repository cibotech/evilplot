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

object DefaultColors {
  val backgroundColor = HSL(0, 0, 92)
  val barColor = HSL(0, 0, 35)
  val titleBarColor = HSL(0, 0, 85)
  val fillColor: Color = HTMLNamedColors.white
  val pathColor = HSL(0, 0, 0)

  val lightPalette: Seq[HSLA] = Seq(
    RGB(26, 188, 156),
    RGB(46, 204, 113),
    RGB(52, 152, 219),
    RGB(155, 89, 182),
    RGB(52, 73, 94),
    RGB(241, 196, 15),
    RGB(230, 126, 34),
    RGB(231, 76, 60)
  )

  val darkPalette: Seq[HSLA] = Seq(
    RGB(22, 160, 133),
    RGB(39, 174, 96),
    RGB(41, 128, 185),
    RGB(142, 68, 173),
    RGB(44, 62, 80),
    RGB(243, 156, 18),
    RGB(211, 84, 0),
    RGB(192, 57, 43)
  )

  @deprecated(
    "This palette contains two palettes, a light and dark. " +
      "Using it as a single palette can be misleading/confusing. Use DefaultColors#lightPalette " +
      "or DefaultColors#darkPalette instead.",
    since = "29 March 2018"
  )
  val nicePalette: Seq[HSLA] = lightPalette ++ darkPalette

}
