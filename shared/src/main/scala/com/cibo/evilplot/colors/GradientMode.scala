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
      if (d <= 0.04045) d / 12.92  else math.pow((d + 0.055) / 1.055, 2.4)
  }

  /** A simple linear gradient in RGB color space. */
  case object Linear extends GradientMode {
    private[colors] def forward(d: Double): Double = d
    private[colors] def inverse(d: Double): Double = d
  }
}
