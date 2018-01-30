/*
 * Copyright 2017 CiBO Technologies
 */

package com.cibo.evilplot.plot

import com.cibo.evilplot.Utils
import com.cibo.evilplot.colors.{Color, ColorBar, ScaledColorBar, SingletonColorBar}
import com.cibo.evilplot.geometry._

case class GradientLegend(gradientBar: ScaledColorBar, height: Double = 150) {
  def drawable: Drawable = {
    val paletteHeight = height / gradientBar.nColors
    Text(Utils.createNumericLabel(gradientBar.zMax, 2)) above
      Align.middle(gradientBar.colorSeq.reverseMap(color => Rect(10, paletteHeight) filled color): _*).reduce(Above.apply) above
      Text(Utils.createNumericLabel(gradientBar.zMin, 2))
  }
}

case class Legend[T](
  colorBar: ColorBar, categories: Seq[T],
  shape: Drawable,
  colorWith: (Drawable, Color) => Drawable,
  backgroundRectangle: Option[Color] = None
) {

  private val categoriesColors = categories.zipWithIndex.map { case (category, index) =>
    colorBar match {
      case SingletonColorBar(color) => (category, color)
      case _colorBar@ScaledColorBar(colors, _, _) =>
        require(colors.length == categories.length, "Color bar must have exactly as many colors as category list.")
        (category, _colorBar.getColor(index))
    }
  }

  private lazy val points = categoriesColors.map { case (label, color) =>
    val point = colorWith(shape, color)
    Align.middle(point, Text(label.toString) padLeft 4 padBottom point.extent.height / 2).reduce(Beside.apply)
  }
  def drawable: Drawable = points.seqDistributeV(shape.extent.height + 10)
}

// TODO: fix the padding fudge factors
case class HorizontalTick(length: Double, thickness: Double, label: Option[String] = None) {
  private val line = Line(length, thickness)

  def drawable: Drawable = label match {
    case Some(_label) => Align.middle(Text(_label).padRight(2).padBottom(2), line).reduce(Beside.apply)
    case None => line
  }
}

case class VerticalTick(length: Double, thickness: Double, label: Option[String] = None, rotateText: Double = 0) {
  private val line = Line(length, thickness).rotated(90)

  def drawable: Drawable = label match {
    case Some(_label) => Align.center(line, (Text(_label) rotated rotateText).padTop(2)).reduce(Above.apply)
    case None => line
  }
}


