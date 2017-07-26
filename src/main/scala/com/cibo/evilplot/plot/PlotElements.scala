/*
 * Copyright 2017 CiBO Technologies
 */

package com.cibo.evilplot.plot

import com.cibo.evilplot.Text
import com.cibo.evilplot.colors.{Black, Color, HSL}
import com.cibo.evilplot.geometry.{Above, Align, Beside, Drawable, Extent, Line, WrapDrawable}
import com.cibo.evilplot.numeric.Ticks

case class PlotOptions(title: Option[String] = None,
                       xAxisBounds: Option[Bounds] = None,
                       yAxisBounds: Option[Bounds] = None,
                       annotation: Option[ChartAnnotation] = None,
                       numXTicks: Option[Int] = None,
                       numYTicks: Option[Int] = None,
                       xGridSpacing: Option[Double] = None,
                       yGridSpacing: Option[Double] = None,
                       withinMetrics: Option[Double] = None,
                       backgroundColor: Color = HSL(0, 0, 92),
                       barWidth: Option[Double] = None,
                       barColor: Color = HSL(0, 0, 35))

case class Bounds(min: Double, max: Double)

/* Base trait for axes. */
trait ChartAxis extends WrapDrawable {
  val minValue: Double
  val maxValue: Double
  lazy val axisRange: Double = maxValue - minValue
  protected val nTicks: Int
  private[plot] val principalDimension: Double // e.g. either length or width
  private[plot] val (tickMin, tickMax, spacing, numFrac) = Ticks.niceTicks(minValue, maxValue, nTicks)
  private[plot] val numTicks: Int = math.round((tickMax - tickMin) / spacing).toInt + 1
  protected val tickThick = 1
  protected val tickLength = 5

  private[plot] lazy val pixelsPerUnit: Double = principalDimension / axisRange

  // Kinda hacky...
  def createNumericLabel(num: Double, numFrac: Int): String = {
    val fmtString = "%%.%df".format(numFrac)
    fmtString.format(num)
  }
  def getLinePosition(coord: Double): Double = (coord - minValue) * pixelsPerUnit

}

class XAxis(extent: Extent, val minValue: Double, val maxValue: Double, val nTicks: Int,
                          drawTicks: Boolean = true) extends ChartAxis {
  private[plot] val chartWidth = extent.width
  private[plot] val chartHeight = extent.height
  private[plot] val principalDimension = chartWidth

  private val ticks = for {
    numTick <- 0 until numTicks
    coordToDraw = tickMin + numTick * spacing
    label = createNumericLabel(coordToDraw, numFrac)
    tick = new VerticalTick(tickLength, tickThick, Some(label))

    padLeft = getLinePosition(coordToDraw) - tick.extent.width / 2.0
  } yield tick padLeft padLeft

  override def drawable: Drawable = ticks.group
}

class YAxis(extent: Extent, val minValue: Double, val maxValue: Double, val nTicks: Int,
                    drawTicks: Boolean = true) extends ChartAxis {
  private[plot] val chartWidth = extent.width
  private[plot] val chartHeight = extent.height
  private[plot] val principalDimension = extent.height

  private val ticks = for {
    numTick <- (numTicks - 1) to 0 by -1
    coordToDraw = tickMin + numTick * spacing
    label = createNumericLabel(coordToDraw, numFrac)
    tick = new HorizontalTick(tickLength, tickThick, Some(label))

    padTop = chartHeight - getLinePosition(coordToDraw) - tick.extent.height / 2.0
  } yield tick padTop padTop

  override def drawable: Drawable = ticks.group
}

trait GridLines extends WrapDrawable {
  protected val axis: ChartAxis
  val axisRange: Double = axis.axisRange
  protected val pixelsPerUnit: Double = axis.pixelsPerUnit
  val spacing: Double
  private[plot] val nLines: Int = math.ceil(axisRange / spacing).toInt

  // Calculate the coordinate of the first grid line to be drawn.
  private val maxNumLines = math.ceil((axis.tickMin - axis.minValue) / spacing).toInt
  protected val minGridLineCoord: Double = {
    if (maxNumLines == 0) axis.minValue
    else List.tabulate[Double](maxNumLines)(axis.tickMin - _ * spacing).filter(_ >= axis.minValue).min
  }

  val minLine: Double = axis.getLinePosition(minGridLineCoord)
  val linePadding: Double = spacing * pixelsPerUnit
}

class VerticalGridLines(val axis: XAxis, val spacing: Double, color: Color = Black) extends GridLines {
  require(nLines != 0)
  private[plot] val lines = for {
    nLine <- 0 until nLines
    line = Line(axis.chartHeight, 1) rotated 90 colored color
    lineWidthCorrection = line.extent.width / 2.0
    padding = axis.getLinePosition(minGridLineCoord + nLine * spacing) - lineWidthCorrection
  } yield {
    line transY -axis.chartHeight padLeft padding
  }

  override def drawable: Drawable = lines.group
}

class HorizontalGridLines(val axis: YAxis, val spacing: Double, color: Color = Black) extends GridLines {
  require(nLines != 0)
  private val lines = for {
    nLines <- (nLines - 1) to 0 by -1
    line = Line(axis.chartWidth, 1) colored color
    lineCorrection = line.extent.height / 2.0
    padding = axis.principalDimension - axis.getLinePosition(minGridLineCoord + nLines * spacing) - lineCorrection
  } yield line padTop padding

  override def drawable: Drawable = lines.group
}

// TODO: Labeling these vertical lines in a way that doesn't mess up their positioning!
class MetricLines(linesToDraw: Seq[Double], xAxis: ChartAxis, bars: Bars,
                                color: Color) extends WrapDrawable {
  val length: Double = bars.nBars * (bars._barWidth + bars.barSpacing)
  val chartHeight: Double = bars.extent.height
  val pixelsPerUnit: Double = length.toDouble / (xAxis.maxValue - xAxis.minValue)
  val lines: Drawable = (for {
    line <- linesToDraw
    padLeft = (line - xAxis.minValue) * pixelsPerUnit
  //      label = f"$line%.1f%%"
  } yield {
    //      Align.center(label, Line(chartHeight - label.extent.height, 2)
    //      colored color rotated 90).reduce(Above).padLeft(padLeft)
    Line(chartHeight, 2) colored color rotated 90 padLeft padLeft
  }).group

  override def drawable: Drawable = lines
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

class VerticalTick(length: Double, thickness: Double, label: Option[String] = None)
  extends WrapDrawable {
  private val line = Line(length, thickness).rotated(90)

  override def drawable: Drawable = label match {
    case Some(_label) => Align.center(line, Text(_label).padTop(2)).reduce(Above)
    case None => line
  }
}


