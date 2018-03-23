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
