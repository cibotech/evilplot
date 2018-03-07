package com.cibo.evilplot.plot.aesthetics

import com.cibo.evilplot.colors.{Color, HSL, HTMLNamedColors}

object PainfulTheme {

  case class PainfulColors(
    background: Color = HTMLNamedColors.blue,
    bar: Color = HTMLNamedColors.red,
    fill: Color = HTMLNamedColors.white,
    path: Color = HTMLNamedColors.blue,
    gridLine: Color = HTMLNamedColors.red,
    trendLine: Color = HTMLNamedColors.red,
    title: Color = HTMLNamedColors.red,
    label: Color = HTMLNamedColors.red,
    annotation: Color = HTMLNamedColors.red,
    legendLabel: Color = HTMLNamedColors.blue,
    tickLabel: Color = HTMLNamedColors.blue,
    point: Color = HTMLNamedColors.red,
    stream: Seq[Color] = Color.stream
  ) extends Colors

  implicit val painfulTheme: Theme = DefaultTheme.DefaultTheme(
    colors = PainfulColors()
  )

}
