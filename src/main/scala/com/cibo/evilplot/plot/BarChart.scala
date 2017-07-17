/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.plot

import com.cibo.evilplot.colors._
import com.cibo.evilplot.geometry._
import com.cibo.evilplot.numeric.Ticks
import com.cibo.evilplot.{Text, colors}
import org.scalajs.dom.CanvasRenderingContext2D

// Should be able to draw either a histogram with an x-axis that directly labels the bins or
// a histogram that has an extended x-axis and plots the data in that context.
class BarChart(override val extent: Extent, xBounds: Option[(Double, Double)], data: Seq[Double],
               title: Option[String] = None, vScale: Double = 1.0, withinMetrics: Option[Double],
               annotation: Option[ChartAnnotation]) extends Drawable {

  val textAndPadHeight: Int = Text.defaultSize + 5 // text size, stroke width
  val xAxisBounds = Option(-75.0, 225.0)

  private val barChart = Fit(extent) {
    val bars = new Bars(xBounds, xAxisBounds, data, vScale)
    // For now assume minValue is zero, we'll need to fix that for graphs that go negative
    //val minValue = data.reduce[Double](math.min)
    val minValue = 0.0
    val maxValue = data.reduce[Double](math.max)
    val gridSpacing: Option[Int] = Some(2)
    val xGridSpacing: Option[Double] = Some(50)
    val yAxisPadding = 20

    val xAxis: Option[XAxis] = xBounds match {
      case Some((xMin, xMax)) => xAxisBounds match {
        case Some((xAxisMin, xAxisMax)) => Some(new XAxis(bars, xAxisMin, xAxisMax, vScale, 3))
        case None => Some(new XAxis(bars, xMin, xMax, vScale, data.length))
      }
      case None => None
    }

    val yAxis = new YAxis(bars, minValue, maxValue, vScale, 10, gridSpacing)

    val xMetricLines = withinMetrics match {
      case Some(metric) if xAxis.isDefined =>
        new MetricLines(Seq(-metric, metric), xAxis.get, bars, vScale, Red)
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

    val chart = (xAxisAndVerticalLines below bars behind xMetricLines) padLeft yAxisPadding behind yAxis

    (title match {
      case Some(_title) => chart titled(_title, 20) padAll 10
      case None => chart
    }) behind translatedAnnotation
  }
  private val assembledChart = (Rect(barChart.extent.width, barChart.extent.height) filled LightGray) behind barChart

  override def draw(canvas: CanvasRenderingContext2D): Unit = assembledChart.draw(canvas)

  private class Bars(dataXBounds: Option[(Double, Double)], drawXBounds: Option[(Double, Double)],
                     heights: Seq[Double], vScale: Double = 1.0) extends WrapDrawable {
    val barWidth = 20
    val barSpacing = 0

    val heightsToDraw: Seq[Double] = (dataXBounds, drawXBounds) match {
      case (Some((dataMax, dataMin)), Some((drawMax, drawMin))) =>
        val binWidth = (dataMax - dataMin) / heights.length
        val additionalBarsOnLeft = math.ceil((dataMin - drawMin) / binWidth).toInt
        val additionalBarsOnRight = math.ceil((drawMax - dataMax) / binWidth).toInt
        Seq.fill[Double](additionalBarsOnRight)(0) ++ heights ++ Seq.fill[Double](additionalBarsOnLeft)(0)
      case _ => heights
    }
    val nBars: Int = heightsToDraw.length
    val bars: Drawable = Align.bottomSeq {
      //      val rects = heightsToDraw.map { h => Rect(barWidth, h * vScale) titled h.toString }
      val rects = heightsToDraw.map { h => Rect(barWidth, h * vScale) }
      rects.map {
        rect => rect filled colors.DarkGrey //labeled color.repr
      }
    }.seqDistributeH(barSpacing)

    override def drawable: Drawable = bars
  }

  /* Base trait for axes. */
  private trait ChartAxis extends WrapDrawable {
    val minValue: Double
    val maxValue: Double
    val vScale: Double
    protected val nTicks: Int
    private[BarChart] val principalDimension: Double // e.g. either length or width
    private[BarChart] val (tickMin, tickMax, spacing, numFrac) = Ticks.niceTicks(minValue, maxValue, nTicks)
    private[BarChart] val firstTickPosition: Option[Double]
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

  private trait GridLines extends WrapDrawable {
    protected val axis: ChartAxis
    val axisRange: Double = axis.maxValue - axis.minValue
    val spacing: Double
    protected val nLines: Int = (axisRange / spacing).toInt

    // Calculate the coordinate of the first grid line to be drawn.
    private val maxNumLines = math.ceil((axis.tickMin - axis.minValue) / spacing).toInt
    private val minGridLineCoord = axis.firstTickPosition match {
      case Some(_) => List.tabulate[Double](maxNumLines)(axis.tickMin - _ * spacing).filter(_ > axis.minValue).min
      case None => axis.minValue
    }

    protected val pixelsPerUnit: Double = axis.principalDimension / axisRange

    def getLinePosition(coord: Double): Double = (coord - axis.minValue) * pixelsPerUnit

    val minLine: Double = getLinePosition(minGridLineCoord)
    val linePadding: Double = spacing * pixelsPerUnit
  }

  private class VerticalGridLines(val axis: XAxis, val spacing: Double, color: Color) extends GridLines {
    override def drawable: Drawable = {
      (for {
        nLine <- 0 until nLines
        line = Line(axis.chartHeight, 1) rotated 90 colored color
        lineWidthCorrection = line.extent.width / 2.0
        padding = if (nLine == 0) minLine - lineWidthCorrection else linePadding - lineWidthCorrection
      } yield {
        line transY -axis.chartHeight padLeft padding
      }).seqDistributeH
    }
  }

  private class XAxis(bars: Bars, val minValue: Double, val maxValue: Double, val vScale: Double, val nTicks: Int,
                      drawTicks: Boolean = true) extends ChartAxis {
    private[BarChart] val chartWidth = bars.extent.width
    private[BarChart] val chartHeight = bars.extent.height
    private[BarChart] val principalDimension = chartWidth
    private val firstTick = new VerticalTick(tickLength, tickThick, Some(createNumericLabel(tickMin, numFrac)))
    private val _firstTickPosition =
      ((tickMin - minValue) / (maxValue - minValue)) * chartWidth + firstTick.extent.width / 2.0
    private[BarChart] val firstTickPosition = Some(_firstTickPosition)

    private val ticks = for {
      numTick <- 0 until numTicks
      label = createNumericLabel(tickMin + numTick * spacing, numFrac)
      tick = new VerticalTick(tickLength, tickThick, Some(label))
    } yield tick

    private val tickWidthCorrections = ticks.map(_.extent.width).scanLeft(0.0)((x, y) => 0.5 * (x + y)).tail

    override def drawable: Drawable = (for {
      (tick, tickWidthCorrection) <- ticks zip tickWidthCorrections
    } yield {
      tick padLeft chartWidth / numTicks - tickWidthCorrection
    }).seqDistributeH padLeft (_firstTickPosition - tickWidthCorrections.head - chartWidth / numTicks)
  }

  // TODO: Split grid line drawing from y-axis drawing, as is done for the x-axis.
  private class YAxis(bars: Bars, val minValue: Double, val maxValue: Double,
                      val vScale: Double, val nTicks: Int, gridLinesEveryNTicks: Option[Int]) extends ChartAxis {
    private val chartWidth = bars.extent.width
    private val chartHeight = bars.extent.height
    private[BarChart] val principalDimension = chartHeight
    private[BarChart] val firstTickPosition = None

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
            case Some(n) => if (numTick % n == 0) Line(chartWidth - tickWidth, 1) colored White else new EmptyDrawable
            case None => new EmptyDrawable
          }
        } yield {
          Align.middle(tick, gridLine).reduce(Beside).padTop(padTop)
        }).seqDistributeV
      }
      override def drawable: Drawable = ticks
    }

  // TODO: Labeling these vertical lines in a way that doesn't mess up their positioning!
  private class MetricLines(linesToDraw: Seq[Double], xAxis: ChartAxis, bars: Bars, vScale: Double,
                            color: Color) extends WrapDrawable {
    val length: Int = bars.nBars * (bars.barWidth + bars.barSpacing)
    val chartHeight: Double = bars.extent.height
    val pixelsPerUnit: Double = length.toDouble / (xAxis.maxValue - xAxis.minValue)
    val lines: Drawable = (for {
      line <- linesToDraw
      padLeft = ((line - xAxis.minValue) * pixelsPerUnit).toInt
//      label = f"$line%.1f%%"
    } yield {
      //      Align.center(label, Line(chartHeight - label.extent.height, 2)
      //      colored color rotated 90).reduce(Above).padLeft(padLeft)
      Line(chartHeight, 2) colored color rotated 90 padLeft padLeft
    }).group

    override def drawable: Drawable = lines
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
      case Some(_label) => Align.center(line, Text(_label).padTop(2)).reduce(Above)
      case None => line
    }
  }
}
