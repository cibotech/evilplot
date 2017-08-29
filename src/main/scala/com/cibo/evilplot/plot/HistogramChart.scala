/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.plot

import com.cibo.evilplot.Utils
import com.cibo.evilplot.colors._
import com.cibo.evilplot.geometry._
import com.cibo.evilplot.layout.ChartLayout
import com.cibo.evilplot.numeric.{AxisDescriptor, Histogram}
import com.cibo.evilplot.plot.ContinuousChartDistributable._
import org.scalajs.dom.CanvasRenderingContext2D

class HistogramData(data: Seq[Double], numBins: Int) extends PlotData {
  override def xBounds: Option[Bounds] = Some(Bounds(data.min, data.max))
  override def createPlot(extent: Extent, options: PlotOptions): Drawable = {
    val hist = new Histogram(data, numBins, bounds = options.xAxisBounds)
    val histData = hist.bins.map(_.toDouble)
    new HistogramChart(extent, options.xAxisBounds, histData, options)
  }
}

// Should be able to draw either a histogram with an x-axis that directly labels the bins or
// a histogram that has an extended x-axis and plots the data in that context.

// xBounds: the minimum and maximum x-value of the histogram.
class HistogramChart(override val extent: Extent, xBounds: Option[Bounds], data: Seq[Double], options: PlotOptions)
  extends Drawable {
  val layout: Drawable = {
    val minValue = 0.0
    val maxValue = data.reduce[Double](math.max)
    val xAxisDrawBounds: Bounds =
      options.xAxisBounds.getOrElse(xBounds
        .getOrElse(throw new IllegalArgumentException("xAxisDrawBounds must be defined")))
    val yAxisDrawBounds: Bounds = options.yAxisBounds.getOrElse(Bounds(minValue, maxValue))

    val topLabel: DrawableLater = Utils.maybeDrawableLater(options.topLabel, (text: String) => Label(text))
    val rightLabel: DrawableLater = Utils.maybeDrawableLater(options.rightLabel,
      (text: String) => Label(text, rotate = 90))

    val xAxisDescriptor = AxisDescriptor(xAxisDrawBounds, options.numXTicks.getOrElse(10))
    val yAxisDescriptor = AxisDescriptor(yAxisDrawBounds, options.numYTicks.getOrElse(10))
    val bars = Bars(xBounds, Some(xAxisDescriptor.axisBounds), yAxisDescriptor.axisBounds, data, options.barColor)
    val xAxis = ContinuousChartDistributable.XAxis(xAxisDescriptor, label = options.xAxisLabel, options.drawXAxis)
    val yAxis = ContinuousChartDistributable.YAxis(yAxisDescriptor, label = options.yAxisLabel, options.drawYAxis)
    val chartArea: DrawableLater = {
      def chartArea(extent: Extent): Drawable = {
        val translatedAnnotation = Utils.maybeDrawable(options.annotation,
          (annotation: ChartAnnotation) =>
            ((annotation transX annotation.position._1 * extent.width)
              transY (annotation.position._2 * extent.height)))
        val xGridLines = Utils.maybeDrawable(options.xGridSpacing,
          (xGridSpacing: Double) => ContinuousChartDistributable
            .VerticalGridLines(xAxisDescriptor, xGridSpacing, color = White)(extent))
        val yGridLines = Utils.maybeDrawable(options.yGridSpacing,
          (yGridSpacing: Double) => ContinuousChartDistributable
            .HorizontalGridLines(yAxisDescriptor, yGridSpacing, color = White)(extent))
        Rect(extent) filled options.backgroundColor behind
          bars(extent) behind xGridLines behind yGridLines behind
          MetricLines(xAxisDescriptor, Seq(-15, 15), Red)(extent) behind translatedAnnotation titled(
            options.title.getOrElse(""), 20.0)
      }
      new DrawableLaterMaker(chartArea)
    }
    val centerFactor = 0.85   // proportion of the plot to allocate to the center
    new ChartLayout(extent, preferredSizeOfCenter = extent * centerFactor, center = chartArea, left = yAxis,
      bottom = xAxis, top = topLabel, right = rightLabel)
  }

  override def draw(canvas: CanvasRenderingContext2D): Unit = layout.draw(canvas)
}

// This is histogram specific. Histograms might be sufficiently different from categorical charts to not be able
// to reasonably implement these the same way. See:
// https://statistics.laerd.com/statistical-guides/understanding-histograms.php
// Given a (physical) width of a chart, (numeric) width of bins, and number of bins for a histogram, the bin width is
// non-negotiable.
case class Bars(dataXBounds: Option[Bounds],
                drawXBounds: Option[Bounds], drawYBounds: Bounds,
                heights: Seq[Double], color: Color) extends DrawableLater {
  val numBins: Int = heights.length

  lazy val heightsToDraw: Seq[Double] = {
    def trimOrExtendHeights(_left: Double, _right: Double, binWidth: Double): Seq[Double] = {
      val left = math.ceil(_left).toInt
      val right = math.ceil(_right).toInt
      val heightsAdjustedOnLeft = if (left >= 0) Seq.fill[Double](left)(0) ++ heights else heights.drop(-left)
      if (right >= 0) heightsAdjustedOnLeft ++ Seq.fill[Double](right)(0)
      else heightsAdjustedOnLeft.take(heightsAdjustedOnLeft.length + right)
    }

    dataXBounds match {
      case Some(Bounds(dataMin, dataMax)) =>
        val binWidth = (dataMax - dataMin) / numBins
        drawXBounds match {
          case Some(Bounds(drawMin, drawMax)) =>
            // TODO: Incorporate these facts: math.ceil on negative => toward 0, math.floor is opposite.
            // Generate offsets from fractional parts of the number of bins? -> Maybe
            val addOrRemoveBarsOnLeft = (dataMin - drawMin) / binWidth
            val addOrRemoveBarsOnRight = (drawMax - dataMax) / binWidth
            trimOrExtendHeights(addOrRemoveBarsOnLeft, addOrRemoveBarsOnRight, binWidth)
          case None => heights
        }
      case None => heights
    }
  }
  def apply(extent: Extent): Drawable = {
    val barSpacing = 0
    // barWidth has a meaning in terms of the interpretation of the plot.
    val barWidth = extent.width / heightsToDraw.length
    val vScale: Double = extent.height / drawYBounds.max
    val bars: Drawable = Align.bottomSeq {
      heightsToDraw.map { h => Rect(barWidth, h * vScale) filled color
        //        if (h != 0) Rect(barWidth, h * vScale) filled color
        //        else Rect(barWidth, extent.height) filled GreenYellow
      }
    }.seqDistributeH(barSpacing)

    Align.bottom(bars, Rect(1, extent.height) filled Clear).reduce(Beside)
  }
}

