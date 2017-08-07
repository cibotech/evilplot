/*
 * Copyright 2017 CiBO Technologies
 */

package com.cibo.evilplot.plot

import com.cibo.evilplot.{Text, Utils}
import com.cibo.evilplot.colors.Colors.{ColorBar, GradientColorBar, SingletonColorBar}
import com.cibo.evilplot.colors.{Black, Color, HSL, White}
import com.cibo.evilplot.geometry._
import com.cibo.evilplot.numeric.Ticks

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
      case _colorBar@GradientColorBar(nColors, _, _) =>
        require(nColors == categories.length, "Color bar must have exactly as many colors as category list.")
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

/* Base trait for axes and grid lines. */
trait ChartDistributable extends DrawableLater {
  private[plot] val ticks: Ticks
  protected val tickThick = 1
  protected val tickLength = 5
  private[plot] lazy val bounds: Bounds = ticks.bounds
  private[plot] def pixelsPerUnit(distributableDimension: Double): Double = distributableDimension / bounds.range

  // TODO: Move this somewhere else where it could be used by things other than axes.
  def createNumericLabel(num: Double, numFrac: Int): String = {
    val fmtString = "%%.%df".format(numFrac)
    fmtString.format(num)
  }
  def getLinePosition(coord: Double, distributableDimension: Double): Double =
    (coord - bounds.min) * pixelsPerUnit(distributableDimension)

}

case class XAxis(ticks: Ticks, label: Option[String] = None, drawTicks: Boolean = true) extends ChartDistributable {
  def apply(extent: Extent): Drawable = {
    lazy val text = Utils.maybeDrawable(label, (msg: String) => Text(msg, 22))
    val _ticks = (for {
      numTick <- 0 until ticks.numTicks
      coordToDraw = ticks.tickMin + numTick * ticks.spacing
      label = createNumericLabel(coordToDraw, ticks.numFrac)
      tick = new VerticalTick(tickLength, tickThick, Some(label))

      padLeft = getLinePosition(coordToDraw, extent.width) - tick.extent.width / 2.0
    } yield tick padLeft padLeft).group
    lazy val _drawable = Align.center(_ticks, text).reduce(Above)
    if (drawTicks) _drawable else EmptyDrawable()
  }
}

case class YAxis(ticks: Ticks, label: Option[String] = None, drawTicks: Boolean = true) extends ChartDistributable {
  def apply(extent: Extent): Drawable = {
    lazy val text = Utils.maybeDrawable(label, (msg: String) => Text(msg, 22) rotated 270)
    val _ticks = for {
      numTick <- (ticks.numTicks - 1) to 0 by -1
      coordToDraw = ticks.tickMin + numTick * ticks.spacing
      label = createNumericLabel(coordToDraw, ticks.numFrac)
      tick = new HorizontalTick(tickLength, tickThick, Some(label))

      padTop = extent.height - getLinePosition(coordToDraw, extent.height) - tick.extent.height / 2.0
    } yield tick padTop padTop

    lazy val _drawable = Align.middle(text, Align.rightSeq(_ticks).group).reduce(Beside)
    if (drawTicks) _drawable else EmptyDrawable()
  }
}


trait GridLines extends ChartDistributable {
  val lineSpacing: Double
  private[plot] val nLines: Int = math.ceil(ticks.bounds.range / lineSpacing).toInt

  // Calculate the coordinate of the first grid line to be drawn.
  private val maxNumLines = math.ceil((ticks.tickMin - ticks.bounds.min) / lineSpacing).toInt
  protected val minGridLineCoord: Double = {
    if (maxNumLines == 0) ticks.bounds.min
    else List.tabulate[Double](maxNumLines)(ticks.tickMin - _ * lineSpacing).filter(_ >= ticks.bounds.min).min
  }
}

case class VerticalGridLines(ticks: Ticks, lineSpacing: Double, color: Color = Black) extends GridLines {
  def apply(extent: Extent): Drawable = {
    require(nLines != 0)
    val lines = for {
      nLine <- 0 until nLines
      line = Line(extent.height, 1) rotated 90 colored color
      lineWidthCorrection = line.extent.width / 2.0
      padding = getLinePosition(minGridLineCoord + nLine * lineSpacing, extent.width) - lineWidthCorrection
    } yield {
      line padLeft padding
    }
    lines.group
  }
}

case class HorizontalGridLines(ticks: Ticks, lineSpacing: Double, color: Color = Black) extends GridLines {
  def apply(extent: Extent): Drawable = {
  require(nLines != 0)
  val lines = for {
    nLines <- (nLines - 1) to 0 by -1
    line = Line(extent.width, 1) colored color
    lineCorrection = line.extent.height / 2.0
    padding = extent.height - getLinePosition(minGridLineCoord + nLines * lineSpacing, extent.height) - lineCorrection
  } yield line padTop padding
    lines.group
  }
}

// TODO: Labeling these vertical lines in a way that doesn't mess up their positioning!
case class MetricLines(ticks: Ticks, linesToDraw: Seq[Double], color: Color) extends ChartDistributable {
  def apply(extent: Extent): Drawable = {
    val lines = for {
      line <- linesToDraw
      padLeft = (line - ticks.bounds.min) * pixelsPerUnit(extent.width)
    } yield Line(extent.height, 2) colored color rotated 90 padLeft padLeft
    lines.group
  }
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

class VerticalTick(length: Double, thickness: Double, label: Option[String] = None)
  extends WrapDrawable {
  private val line = Line(length, thickness).rotated(90)

  override def drawable: Drawable = label match {
    case Some(_label) => Align.center(line, Text(_label).padTop(2)).reduce(Above)
    case None => line
  }
}


