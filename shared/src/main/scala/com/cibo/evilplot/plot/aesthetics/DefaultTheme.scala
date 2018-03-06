package com.cibo.evilplot.plot.aesthetics

import com.cibo.evilplot.colors.{Color, HSL, HTMLNamedColors}

object DefaultTheme {

  case class DefaultFonts(
    titleSize: Double = 22,
    labelSize: Double = 20,
    annotationSize: Double = 20
  ) extends Fonts

  case class DefaultColors(
    background: Color = HSL(0, 0, 92),
    bar: Color = HSL(0, 0, 35),
    fill: Color = HTMLNamedColors.white,
    path: Color = HSL(0, 0, 0),
    gridLine: Color = HTMLNamedColors.white,
    trendLine: Color = HSL(0, 0, 35)
  ) extends Colors

  case class DefaultElements(
    strokeWidth: Double = 2,
    pointSize: Double = 2.5
  ) extends Elements

  case class DefaultTheme(
    fonts: Fonts = DefaultFonts(),
    colors: Colors = DefaultColors(),
    elements: Elements = DefaultElements()
  ) extends Theme

  implicit val defaultTheme: Theme = DefaultTheme()

}
