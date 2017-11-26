package com.cibo.evilplot.colors

private[colors] object ColorUtils{

  private def calculateHueFromRGB(max: Double, min: Double, red: Double, green: Double, blue: Double): Double = {
    if (max == min) {
      0.0
    } else {
      val delta = max - min

      val h = max match {
        case rd if rd == red   => ((green - blue) / delta) + (if (green < blue) 6 else 0)
        case gr if gr == green => (blue - red) / delta + 2
        case bl if bl == blue  => (red - green) / delta + 4
      }

      h / 6
    }
  }

  private def calculateSaturationFromMagnitude(max: Double, min: Double): Double = {
    if (max == min) {
      0.0
    } else {
      val delta = max - min

      if ((max + min) / 2 > 0.5) delta / (2 - max - min)
      else delta / (max + min)
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

    val h = calculateHueFromRGB(max, min, red, green, blue)
    val s = calculateSaturationFromMagnitude(max, min)
    val l = (max + min) / 2

    HSLA(
      hue = Math.round(h * 360).toInt,
      saturation = Math.round(s * 100).toInt,
      lightness = Math.round(l * 100).toInt,
      transparency = a
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
