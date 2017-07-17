/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.plot

import com.cibo.evilplot.colors.{Color, Grey, Red}
import com.cibo.evilplot.geometry._
import com.cibo.evilplot.numeric.Ticks
import com.cibo.evilplot.{Text, colors}
import org.scalajs.dom.CanvasRenderingContext2D


class BarChart(override val extent: Extent, xBounds: Option[(Double, Double)], data: Seq[Double],
               title: Option[String] = None, vScale: Double = 1.0, withinMetrics: Option[Double]) extends Drawable {

  val textAndPadHeight = Text.defaultSize + 5 // text size, stroke width

  private val barChart = Fit(extent) {
    val bars = new Bars(data, vScale)
    // For now assume minValue is zero, we'll need to fix that for graphs that go negative
    //val minValue = data.reduce[Double](math.min)
    val minValue = 0.0
    val maxValue = data.reduce[Double](math.max)
    val gridSpacing: Option[Int] = Some(2)

    val yAxisPadding = 20

    val _xAxis: Option[XAxis] = xBounds match {
      case Some(_xBounds) => Some(new XAxis(bars.extent.width, _xBounds._1, _xBounds._2, vScale, data.length,
                                      bars.barWidth, bars.barSpacing))
      case None => None
    }

    // TODO: Fix y-axis number of ticks provided.
    val _yAxis = new YAxis(bars.extent.height, bars.extent.width, minValue, maxValue, vScale, 10, gridSpacing)

    val _xGridLines = withinMetrics match {
      case Some(metric) if _xAxis.isDefined =>
        new MetricLines(Seq(-metric, metric), _xAxis.get, data.length, bars, vScale, Red)
      case None => new EmptyDrawable
    }

    val chart = _xAxis match {
      case Some(__xAxis) => (((__xAxis below bars) behind _xGridLines) padLeft yAxisPadding) behind _yAxis
      case None => ((bars behind _xGridLines) padLeft yAxisPadding) behind _yAxis
    }

    title match {
      case Some(_title) => chart titled (_title, 20) padAll 10
      case None => chart
    }
  }

  override def draw(canvas: CanvasRenderingContext2D): Unit = barChart.draw(canvas)

  private class Bars(heights: Seq[Double], vScale: Double = 1.0) extends WrapDrawable {
    val barWidth = 50
    val barSpacing = 0
    val bars: Drawable = Align.bottomSeq {
      val rects = heights.map { h => Rect(barWidth, h * vScale) titled h.toString }
      rects.map {
        rect => rect filled colors.DarkGrey //labeled color.repr
      }
    }.seqDistributeH(barSpacing)

    override def drawable: Drawable = bars
  }

  /* Base trait for axes and gridlines. */
  private trait Axis extends WrapDrawable {
    val minValue: Double
    val maxValue: Double
    val vScale: Double
    protected val nTicks: Int
    protected val (tickMin, tickMax, spacing, numFrac) = Ticks.niceTicks(minValue, maxValue, nTicks)
    protected val numTicks: Int = math.round((tickMax - tickMin) / spacing).toInt + 1
    protected val tickThick = 1
    protected val tickLength = 5
    protected val tickMaxScaled: Double = tickMax * vScale
    protected val spacingScaled: Double = spacing * vScale

    // will fail if 2^-31 <= num < 2^31 is false. there are a lot of problems with this method...
    // TODO: actually employ the numFrac variable to create good tick labels
    def createNumericLabel(num: Double, numFrac: Int): String = {
      if (numFrac == 0) num.toInt.toString
      else f"$num%.4f"
    }
  }

  private class XAxis(width: Double, val minValue: Double, val maxValue: Double, val vScale: Double, val nTicks: Int,
                      barWidth: Int, barSpacing: Int) extends Axis {
    private val ticks = {
      (for {
        numTick <- 0 until nTicks
        label = createNumericLabel(tickMin + numTick * spacing, numFrac)
        tick = new VerticalTick(tickLength, tickThick, Some(label))
        tickWidth = tick.extent.width
        padLeft = if (numTick == 0)
          barWidth / 2.0
        else
          barWidth + barSpacing - tickWidth
      } yield {
        tick.padLeft(padLeft)
      }).seqDistributeH
    }

    override def drawable: Drawable = ticks
  }

  // TODO: Split grid line drawing from y-axis drawing.
  private class YAxis(chartHeight: Double, chartWidth: Double, val minValue: Double, val maxValue: Double,
                      val vScale: Double, val nTicks: Int, gridLinesEveryNTicks: Option[Int]) extends Axis {
    private val ticks = {
      (for {
      // The graphics origin is in the top left, so we are walking the ticks in reverse order
        numTick <- (numTicks - 1) to 0 by -1
        label = (tickMin + (numTick * spacing)).toString
        tick = new HorizontalTick(tickLength, tickThick, Some(label))
        tickHeight = tick.extent.height
        tickWidth = tick.extent.width
        padTop = if (numTick == numTicks - 1)
          chartHeight - tickMaxScaled - (tickHeight / 2.0)
        else
          spacingScaled - tickHeight
        gridLine = gridLinesEveryNTicks match {
          case Some(n) => if (numTick % n == 0) Line(chartWidth - tickWidth, 1) colored Grey else new EmptyDrawable
          case None => new EmptyDrawable
        }
      } yield {
        Align.middle(tick, gridLine).reduce(Beside).padTop(padTop)
      }).seqDistributeV
    }
    override def drawable: Drawable = ticks
  }

  /* Draw vertical lines at x = all points in the sequence linesToDraw */
  private class MetricLines(linesToDraw: Seq[Double], xAxis: XAxis, nBars: Int, bars: Bars,
                            vScale: Double, color: Color) extends WrapDrawable {
    val length = nBars * (bars.barWidth + bars.barSpacing)
    val chartHeight = bars.extent.height
    val pixelsPerUnit = length.toDouble / (xAxis.maxValue - xAxis.minValue)
    override def drawable: Drawable = (for {
      line <- linesToDraw
      padLeft = ((line - xAxis.minValue) * pixelsPerUnit).toInt
    } yield {
      (Line(chartHeight, 2) colored color rotated 90).padLeft(padLeft)
    }).group
  }

  // TODO: fix the padding fudge factors
  private class HorizontalTick(length: Double, thickness: Double, label: Option[String] = None) extends WrapDrawable {
    private val line = Line(length, thickness)

    override def drawable: Drawable = label match {
      case Some(_label) => Align.middle(Text(_label).padRight(2).padBottom(2), line).reduce(Beside)
      case None => line
    }
  }

  private class VerticalTick(length: Double, thickness: Double, label: Option[String] = None) extends WrapDrawable {
    private val line = Line(length, thickness).rotated(90)
    override def drawable: Drawable = label match {
      case Some(_label) => Align.middle(line, Text(_label).rotated(60).padTop(2)).reduce(Above)
      case None => line
    }
  }
}
