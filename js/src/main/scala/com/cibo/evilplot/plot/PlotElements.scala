/*
 * Copyright 2017 CiBO Technologies
 */

package com.cibo.evilplot.plot

import com.cibo.evilplot.{Text, Utils}
import com.cibo.evilplot.colors.Color
import com.cibo.evilplot.colors.Colors.{ColorBar, ScaledColorBar, SingletonColorBar}
import com.cibo.evilplot.geometry._

class GradientLegend(gradientBar: ScaledColorBar, height: Double = 150) extends WrapDrawable {
  def drawable: Drawable = {
    val paletteHeight = height / gradientBar.nColors
    Text(Utils.createNumericLabel(gradientBar.zMax, 2)) above
      Align.middle(gradientBar.colorSeq.reverseMap(color => Rect(10, paletteHeight) filled color): _*).reduce(Above) above
      Text(Utils.createNumericLabel(gradientBar.zMin, 2))
  }
}

class Legend[T](colorBar: ColorBar, categories: Seq[T],
                shape: Drawable, colorWith: Color => Drawable => Drawable,
                backgroundRectangle: Option[Color] = None)
                extends WrapDrawable {

  private val categoriesColors = categories.zipWithIndex.map { case (category, index) =>
    colorBar match {
      case SingletonColorBar(color) => (category, color)
      case _colorBar@ScaledColorBar(colors, _, _) =>
        require(colors.length == categories.length, "Color bar must have exactly as many colors as category list.")
        (category, _colorBar.getColor(index))
    }
  }

  private lazy val points = categoriesColors.map { case (label, color) =>
    val point = colorWith(color)(shape)
    Align.middle(point, Text(label.toString) padLeft 4 padBottom point.extent.height / 2).reduce(Beside)
  }
  def drawable: Drawable = points.seqDistributeV(shape.extent.height + 10)
}

// TODO: fix the padding fudge factors
class HorizontalTick(length: Double, thickness: Double, label: Option[String] = None)
  extends WrapDrawable {
  private val line = Line(length, thickness)

  override def drawable: Drawable = label match {
    case Some(_label) => Align.middle(Text(_label).padRight(2).padBottom(2), line).reduce(Beside)
    case None => line
  }
}

class VerticalTick(length: Double, thickness: Double, label: Option[String] = None, rotateText: Double = 0)
  extends WrapDrawable {
  private val line = Line(length, thickness).rotated(90)

  override def drawable: Drawable = label match {
    case Some(_label) => Align.center(line, (Text(_label) rotated rotateText).padTop(2)).reduce(Above)
    case None => line
  }
}


