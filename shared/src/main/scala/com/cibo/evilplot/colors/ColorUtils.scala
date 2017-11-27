package com.cibo.evilplot.colors

private[colors] object ColorUtils{

  //https://en.wikipedia.org/wiki/Hue
  //https://en.wikipedia.org/wiki/HSL_and_HSV

  private def calculateHueFromRGB(max: Double, min: Double, red: Double, green: Double, blue: Double): Double = {
    if (max == min) {
      0.0
    } else {
      val chroma = max - min

      val hue = max match {
        case redHue   if redHue == red     => (green - blue) / chroma + (if (green < blue) 6 else 0)
        case greenHue if greenHue == green => (blue - red) / chroma + 2
        case blueHue  if blueHue == blue   => (red - green) / chroma + 4
      }

      hue / 6
    }
  }

  private def calculateSaturationFromMagnitude(max: Double, min: Double): Double = {
    if (max == min) {
      0.0
    } else {
      val chroma = max - min
      val lightness = (max + min) / 2

      chroma / (1 - Math.abs(2 * lightness - 1))
    }
  }

  def rgbaToHsla(r: Int, g: Int, b: Int, a: Double): HSLA = {

    require(g >= 0 && g <  256, s"g must be within [0, 256) {was $g}")
    require(g >= 0 && g <  256, s"g must be within [0, 256) {was $g}")
    require(b >= 0 && b <  256, s"b must be within [0, 256) {was $b}")

    val red = r / 255.0
    val green = g / 255.0
    val blue = b / 255.0

    val max = Seq(red, green, blue).max
    val min = Seq(red, green, blue).min

    val hueFraction = calculateHueFromRGB(max, min, red, green, blue)
    val saturationFraction = calculateSaturationFromMagnitude(max, min)
    val lightnessFraction = (max + min) / 2

    HSLA(
      hue = Math.round(hueFraction * 360).toInt,
      saturation = Math.round(saturationFraction * 100).toInt,
      lightness = Math.round(lightnessFraction * 100).toInt,
      opacity = a
    )
  }

  def hexToHsla(hexString: String): HSLA = {

    require(hexString.length > 0, "hex string length <= 0")

    val hexValues = if (hexString.startsWith("#")) hexString.tail else hexString

    require(hexValues.length % 3 == 0 && hexValues.length <= 6, s"invalid hex string: $hexString")

    val split = hexValues.sliding(hexValues.length / 3, hexValues.length / 3)

    val Seq(r, g, b) = if (hexValues.length == 3){
      split.map(Integer.parseInt(_, 16)).map(v => v + v * 16).toSeq
    } else {
      split.map(Integer.parseInt(_, 16)).toSeq
    }

    rgbaToHsla(r, g, b, 1.0)
  }
}
