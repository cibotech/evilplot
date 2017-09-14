/*
 * Copyright 2017 CiBO Technologies
 */

package com.cibo.evilplot.plot

import com.cibo.evilplot.Text
import com.cibo.evilplot.colors.Colors.{ScaledColorBar, ColorBar, SingletonColorBar}
import com.cibo.evilplot.colors.{Color, HSL, White}
import com.cibo.evilplot.geometry._

case class PlotOptions(title: Option[String] = None,
                       xAxisBounds: Option[Bounds] = None,
                       yAxisBounds: Option[Bounds] = None,
                       drawXAxis: Boolean = true,
                       drawYAxis: Boolean = true,
                       annotation: Option[ChartAnnotation] = None,
                       numXTicks: Option[Int] = None,
                       numYTicks: Option[Int] = None,
                       xAxisLabel: Option[String] = None,
                       yAxisLabel: Option[String] = None,
                       topLabel: Option[String] = None,
                       rightLabel: Option[String] = None,
                       xGridSpacing: Option[Double] = None,
                       yGridSpacing: Option[Double] = None,
                       gridColor: Color = White,
                       withinMetrics: Option[Double] = None,
                       backgroundColor: Color = HSL(0, 0, 92),
                       barColor: Color = HSL(0, 0, 35))

case class Bounds(min: Double, max: Double) {
  lazy val range: Double = max - min
  def isInBounds(x: Double): Boolean = x >= min && x <= max
}

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

  private val points = categoriesColors.map { case (label, color) =>
    val point = Disc(pointSize) filled color
    Align.middle(backgroundRectangle match {
      case Some(bc) => Align.centerSeq(Align.middle(Rect(4 * pointSize, 4 * pointSize) filled bc, point)).group
      case None => point
    }, Text(label.toString)).reduce(Beside)
  }

  override def drawable: Drawable = points.seqDistributeV(pointSize)
}



case class Label(message: String, textSize: Option[Double] = None, color: Color = HSL(0, 0, 85), rotate: Double = 0)
  extends DrawableLater {
  def apply(extent: Extent): Drawable = {
    val text = (textSize match {
      case Some(size) => Text(message, size)
      case None => Text(message)
    }) rotated rotate
    Align.centerSeq(Align.middle(Rect(extent) filled color, text)).group
  }
}

//case class HorizontalLabel(message: String, color: Color = HSL(0, 0, 85)) extends DrawableLater {
//  def apply(extent: Extent): Drawable = {
//    val text = Text(message)
//    Align.centerSeq(Align.middle(Rect(extent) filled color, text)).group
//  }
//}

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


