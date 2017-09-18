/*
 * Copyright 2017 CiBO Technologies
 */

package com.cibo.evilplot.plot

import com.cibo.evilplot.Text
import com.cibo.evilplot.colors.Colors.{ColorBar, ScaledColorBar, SingletonColorBar}
import com.cibo.evilplot.colors.{Color, HSL, White}
import com.cibo.evilplot.geometry._
import com.cibo.evilplot.numeric.Bounds

class Legend[T](colorBar: ColorBar, categories: Seq[T],
                pointSize: Double, backgroundRectangle: Option[Color] = None)
               (implicit cmp: Ordering[T]) extends WrapDrawable {

  private val categoriesColors = categories.sorted.zipWithIndex.map { case (category, index) =>
    colorBar match {
      case SingletonColorBar(color) => (category, color)
      case _colorBar@ScaledColorBar(colors, _, _) =>
        require(colors.length == categories.length, "Color bar must have exactly as many colors as category list.")
        (category, _colorBar.getColor(index))
    }
  }

  private lazy val points = categoriesColors.map { case (label, color) =>
    val point = Disc(pointSize) filled color
    Align.middle(backgroundRectangle match {
      case Some(bc) => Align.centerSeq(Align.middle(Rect(4 * pointSize, 4 * pointSize) filled bc, point)).group
      case None => point
    }, Text(label.toString)).reduce(Beside)
  }
//  val test = Text(543534)
  override def drawable: Drawable = points.seqDistributeV(pointSize)
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


