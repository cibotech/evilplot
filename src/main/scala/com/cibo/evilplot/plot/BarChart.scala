/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.plot

import com.cibo.evilplot.colors._
import com.cibo.evilplot.geometry._
import com.cibo.evilplot.numeric.Ticks
import com.cibo.evilplot.Text
import org.scalajs.dom.CanvasRenderingContext2D

// Should be able to draw either a histogram with an x-axis that directly labels the bins or
// a histogram that has an extended x-axis and plots the data in that context.

class BarChart(
  override val extent: Extent,
  xBounds: Option[(Double, Double)],
  data: Seq[Double],
  xAxisDrawBounds: Option[(Double, Double)] = None,
  yAxisDrawBounds: Option[(Double, Double)] = None,
  title: Option[String] = None,
  vScale: Double = 1.0, withinMetrics: Option[Double] = None,
  annotation: Option[ChartAnnotation] = None,
  barWidth: Option[Double] = None,
  xGridSpacing: Option[Double] = None,
  yGridSpacing: Option[Double] = None,
  backgroundColor: Color = HSL(0, 0, 92),
  barColor: Color = HSL(0, 0, 35))
  extends Drawable {
  val textAndPadHeight: Int = Text.defaultSize + 5 // text size, stroke width

  private val barChart = Fit(extent) {
    val minValue = 0.0
    val maxValue = data.reduce[Double](math.max)
    val bars = yAxisDrawBounds match {
      case Some((yMin, yMax)) => new Bars(extent, xBounds, xAxisDrawBounds, (yMin, yMax), data, barColor,
        barWidth = barWidth)
      case None => new Bars(extent, xBounds, xAxisDrawBounds, (minValue, maxValue), data, barColor, barWidth = barWidth)
    }
    // For now assume minValue is zero, we'll need to fix that for graphs that go negative
    // val minValue = data.reduce[Double](math.min)

    val yAxisPadding = 20

    val xAxis: Option[XAxis] = xBounds match {
      case Some((xMin, xMax)) => xAxisDrawBounds match {
        case Some((xAxisMin, xAxisMax)) => Some(new XAxis(bars, xAxisMin, xAxisMax, 3))
        case None => Some(new XAxis(bars, xMin, xMax, data.length))
      }
      case None => None
    }

    val yAxis = yAxisDrawBounds match {
      case Some((yMin, yMax)) => new YAxis(bars, yMin, yMax, 3)
      case None => new YAxis(bars, minValue, maxValue, 3)
    }

    val xMetricLines = withinMetrics match {
      case Some(metric) if xAxis.isDefined =>
        new MetricLines(Seq(-metric, metric), xAxis.get, bars, Red)
      case None => new EmptyDrawable
    }

    val translatedAnnotation = annotation match {
      case Some(_annotation) =>
        ((_annotation transX _annotation.position._1 * bars.extent.width)
          transY (_annotation.position._2 * bars.extent.height))
      case None => new EmptyDrawable
    }

    val xAxisAndVerticalLines = xAxis match {
      case Some(_xAxis) => xGridSpacing match {
        case Some(_xGridSpacing) =>
          val grid = new VerticalGridLines(_xAxis, _xGridSpacing, White)
          _xAxis below grid
        case None => _xAxis
      }
      case None => new EmptyDrawable
    }

    val yAxisAndHorizontalLines = yGridSpacing match {
      case Some(_yGridSpacing) =>
        val grid = new HorizontalGridLines(yAxis, _yGridSpacing, White)
        yAxis beside grid
      case None => yAxis
    }

    val chart = (xAxisAndVerticalLines below bars behind xMetricLines) padLeft
      yAxisPadding behind yAxisAndHorizontalLines

    (title match {
      case Some(_title) => chart titled(_title, 20) padAll 10
      case None => chart
    }) behind translatedAnnotation
  }
  private val assembledChart = (Rect(barChart.extent.width, barChart.extent.height)
    filled backgroundColor) behind barChart

  override def draw(canvas: CanvasRenderingContext2D): Unit = assembledChart.draw(canvas)
}

private[plot] class Bars(chartSize: Extent, dataXBounds: Option[(Double, Double)],
                         drawXBounds: Option[(Double, Double)], drawYBounds: (Double, Double),
                         heights: Seq[Double], color: Color, barWidth: Option[Double] = None) extends WrapDrawable {
  private val heightsToDraw: Seq[Double] = (dataXBounds, drawXBounds) match {
    case (Some((dataMax, dataMin)), Some((drawMax, drawMin))) =>
      val binWidth = (dataMax - dataMin) / heights.length
      val additionalBarsOnLeft = math.ceil((dataMin - drawMin) / binWidth).toInt
      val additionalBarsOnRight = math.ceil((drawMax - dataMax) / binWidth).toInt
      Seq.fill[Double](additionalBarsOnRight)(0) ++ heights ++ Seq.fill[Double](additionalBarsOnLeft)(0)
    case _ => heights
  }
  val nBars: Int = heightsToDraw.length
  val _barWidth: Double = barWidth match {
    case Some(x) => x
    case None => chartSize.width / nBars
  }
  val barSpacing = 0
  val vScale: Double = chartSize.height / drawYBounds._2
  val bars: Drawable = Align.bottomSeq {
    val rects = heightsToDraw.map { h => Rect(_barWidth, h * vScale) }
    rects.map {
      rect => rect filled color
    }
  }.seqDistributeH(barSpacing)

  override def drawable: Drawable = Align.bottom(bars, Rect(1, chartSize.height) filled Clear).reduce(Beside)
}

/* Base trait for axes. */
private[plot] trait ChartAxis extends WrapDrawable {
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

private[plot] trait GridLines extends WrapDrawable {
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

private[plot] class VerticalGridLines(val axis: XAxis, val spacing: Double, color: Color = Black) extends GridLines {
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

private[plot] class HorizontalGridLines(val axis: YAxis, val spacing: Double, color: Color = Black) extends GridLines {
  require(nLines != 0)
  private val lines = for {
    nLines <- (nLines - 1) to 0 by -1
    line = Line(axis.chartWidth, 1) colored color
    lineCorrection = line.extent.height / 2.0
    padding = axis.principalDimension - axis.getLinePosition(minGridLineCoord + nLines * spacing) - lineCorrection
  } yield line padTop padding

  override def drawable: Drawable = lines.group
}


private[plot] class XAxis(bars: Bars, val minValue: Double, val maxValue: Double, val nTicks: Int,
                    drawTicks: Boolean = true) extends ChartAxis {
  private[plot] val chartWidth = bars.extent.width
  private[plot] val chartHeight = bars.extent.height
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

private class YAxis(bars: Bars, val minValue: Double, val maxValue: Double, val nTicks: Int,
                     drawTicks: Boolean = true) extends ChartAxis {
  private[plot] val chartWidth = bars.extent.width
  private[plot] val chartHeight = bars.extent.height
  private[plot] val principalDimension = bars.extent.height

  private val ticks = for {
    numTick <- (numTicks - 1) to 0 by -1
    coordToDraw = tickMin + numTick * spacing
    label = createNumericLabel(coordToDraw, numFrac)
    tick = new HorizontalTick(tickLength, tickThick, Some(label))

    padTop = chartHeight - getLinePosition(coordToDraw) - tick.extent.height / 2.0
  } yield tick padTop padTop

  override def drawable: Drawable = ticks.group
}

// TODO: Labeling these vertical lines in a way that doesn't mess up their positioning!
private[plot] class MetricLines(linesToDraw: Seq[Double], xAxis: ChartAxis, bars: Bars,
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
private[plot] class HorizontalTick(length: Double, thickness: Double, label: Option[String] = None)
  extends WrapDrawable {
  private val line = Line(length, thickness)

  override def drawable: Drawable = label match {
    case Some(_label) => Align.middle(Text(_label).padRight(2).padBottom(2), line).reduce(Beside)
    case None => line
  }
}

private[plot] class VerticalTick(length: Double, thickness: Double, label: Option[String] = None)
  extends WrapDrawable {
  private val line = Line(length, thickness).rotated(90)

  override def drawable: Drawable = label match {
    case Some(_label) => Align.center(line, Text(_label).padTop(2)).reduce(Above)
    case None => line
  }
}
