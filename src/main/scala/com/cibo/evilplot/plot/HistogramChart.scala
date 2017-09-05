/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.plot

import com.cibo.evilplot.colors._
import com.cibo.evilplot.geometry._
import com.cibo.evilplot.layout.ChartLayout
import com.cibo.evilplot.numeric.{AxisDescriptor, Histogram}
import com.cibo.evilplot.plot.ContinuousChartDistributable._
import com.cibo.evilplot.{Style, Utils}
import org.scalajs.dom.CanvasRenderingContext2D

case class HistogramData(data: Seq[Double], numBins: Int, annotation: Seq[String] = Nil, bounds: Option[Bounds] = None)
                                                                                    extends PlotData {
  override def xBounds: Option[Bounds] = Some(Bounds(data.min, data.max))
  val hist = new Histogram(data, numBins, bounds = bounds)
  def histogramBounds(binBounds: Bounds): Bounds = {
    val hist = new Histogram(data, numBins, bounds = Some(binBounds))
    Bounds(hist.bins.min, hist.bins.max)
  }
  override def createPlot(extent: Extent, options: PlotOptions): Drawable = {
    new HistogramChart(extent, this, options)
  }
}

// Should be able to draw either a histogram with an x-axis that directly labels the bins or
// a histogram that has an extended x-axis and plots the data in that context.

// xBounds: the minimum and maximum x-value of the histogram.
class HistogramChart(override val extent: Extent, histData: HistogramData, options: PlotOptions)
  extends Drawable {
  private val xBounds = Bounds(histData.hist.min, histData.hist.max)
  private val data = histData.hist.bins.map(_.toDouble)
  val layout: Drawable = {
    val minValue = 0.0
    val maxValue = data.reduce[Double](math.max)
    val xAxisDrawBounds: Bounds = options.xAxisBounds.getOrElse(xBounds)
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
        val annotation = ChartAnnotation(histData.annotation, (.8, .3))
        val translatedAnnotation = Translate(annotation.position._1 * extent.width,
                                           annotation.position._2 * extent.height)(annotation)
        val xGridLines = Utils.maybeDrawable(options.xGridSpacing,
          (xGridSpacing: Double) => ContinuousChartDistributable
            .VerticalGridLines(xAxisDescriptor, xGridSpacing, color = White)(extent))
        val yGridLines = Utils.maybeDrawable(options.yGridSpacing,
          (yGridSpacing: Double) => ContinuousChartDistributable
            .HorizontalGridLines(yAxisDescriptor, yGridSpacing, color = White)(extent))
        val metricLines = Utils.maybeDrawable(options.withinMetrics,
          (metrics: Seq[Double]) => MetricLines(xAxisDescriptor, metrics, Red)(extent))
        Rect(extent) filled options.backgroundColor behind
          bars(extent) behind xGridLines behind yGridLines behind
          metricLines behind translatedAnnotation
      }
      new DrawableLaterMaker(chartArea)
    }
    val centerFactor = 0.85   // proportion of the plot to allocate to the center
    new ChartLayout(extent, preferredSizeOfCenter = extent * centerFactor, center = chartArea, left = yAxis,
      bottom = xAxis, top = topLabel, right = rightLabel) titled(options.title.getOrElse(""), 20.0)
  }

  override def draw(canvas: CanvasRenderingContext2D): Unit = layout.draw(canvas)
}

case class Bars(dataXBounds: Bounds,
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
    val (dataMin, dataMax) = (dataXBounds.min, dataXBounds.max)
    val binWidth = (dataMax - dataMin) / numBins
    drawXBounds match {
      case Some(Bounds(drawMin, drawMax)) =>
        // TODO: Incorporate these facts: math.ceil on negative => toward 0, math.floor is opposite.
        val addOrRemoveBarsOnLeft = (dataMin - drawMin) / binWidth
        val addOrRemoveBarsOnRight = (drawMax - dataMax) / binWidth
        trimOrExtendHeights(addOrRemoveBarsOnLeft, addOrRemoveBarsOnRight, binWidth)
      case None => heights
    }
  }
  def apply(extent: Extent): Drawable = {
    val barSpacing = 0
    // barWidth has a meaning in terms of the interpretation of the plot.
    val barWidth = extent.width / heightsToDraw.length
    val vScale: Double = extent.height / drawYBounds.range
    heightsToDraw.map { h =>
      Style(color)(Scale(y = vScale)(FlipY(drawYBounds.max)(Rect(barWidth, h))))
      //                if (h != 0) Rect(barWidth, h * vScale) filled color
      //                else Style(GreenYellow)(StrokeStyle(Black)(BorderFillRect(barWidth, extent.height)))
    }.seqDistributeH(barSpacing)
  }
}

