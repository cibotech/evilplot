package com.cibo.evilplot.plot.aesthetics

import com.cibo.evilplot.colors.{Color, HSL, HTMLNamedColors}

object PainfulTheme {

  case class PainfulColors(
    background: Color = HTMLNamedColors.blue,
    bar: Color = HTMLNamedColors.red,
    fill: Color = HTMLNamedColors.white,
    path: Color = HTMLNamedColors.blue,
    gridLine: Color = HTMLNamedColors.red,
    trendLine: Color = HTMLNamedColors.red
  ) extends Colors

  implicit val painfulTheme: Theme = DefaultTheme.DefaultTheme(
    colors = PainfulColors()
  )

}
