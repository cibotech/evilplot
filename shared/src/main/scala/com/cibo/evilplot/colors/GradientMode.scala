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

sealed trait GradientMode {
  private[colors] def forward(d: Double): Double
  private[colors] def inverse(d: Double): Double
}
object GradientMode {

  /** A "natural" gradient transforms sRGB color to the RGB color space,
    * in which linear interpolation is performed to generate a gradient before transforming
    * back to sRGB. This can give better results than a simple linear gradient. */
  case object Natural extends GradientMode {
    // https://stackoverflow.com/questions/22607043/color-gradient-algorithm
    // https://www.w3.org/Graphics/Color/srgb
    private[colors] def forward(d: Double): Double =
      if (d <= 0.0031308) d * 12.92 else 1.055 * math.pow(d, 1.0 / 2.4) - 0.055

    private[colors] def inverse(d: Double): Double =
      if (d <= 0.04045) d / 12.92 else math.pow((d + 0.055) / 1.055, 2.4)
  }

  /** A simple linear gradient in RGB color space. */
  case object Linear extends GradientMode {
    private[colors] def forward(d: Double): Double = d
    private[colors] def inverse(d: Double): Double = d
  }
}
