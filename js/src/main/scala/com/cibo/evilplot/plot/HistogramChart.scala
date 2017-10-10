/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.plot

import com.cibo.evilplot.colors.{Color, HTMLNamedColors}
import com.cibo.evilplot.geometry._
import com.cibo.evilplot.numeric.Bounds
import com.cibo.evilplot.plot.ContinuousChartDistributable.MetricLines
import com.cibo.evilplot.plotdefs.{HistogramChartDef, PlotOptions}
import com.cibo.evilplot.{Style, Utils}

class HistogramChart(override val chartSize: Extent, histData: HistogramChartDef)
  extends Chart with ContinuousAxes {
  val options: PlotOptions = histData.options
  private val data = histData.data.bins.map(_.toDouble)
  val defaultXAxisBounds = Bounds(histData.data.min, histData.data.max)
  val defaultYAxisBounds = Bounds(0.0, data.max)

  def plottedData(extent: Extent): Drawable = {
    val annotation = ChartAnnotation(histData.annotation, (.8, .3))
    val translatedAnnotation = Translate(annotation.position._1 * extent.width,
      annotation.position._2 * extent.height)(annotation)
    val metricLines = Utils.maybeDrawable(options.withinMetrics)(metrics =>
      MetricLines(extent, xAxisDescriptor, metrics, HTMLNamedColors.red))
    val bars = Bars(extent, defaultXAxisBounds, Some(xAxisDescriptor.axisBounds),
      yAxisDescriptor.axisBounds, data, options.barColor)
    bars behind metricLines behind translatedAnnotation
  }
}

case class Bars(chartAreaSize: Extent,
                dataXBounds: Bounds,
                drawXBounds: Option[Bounds], drawYBounds: Bounds,
                heights: Seq[Double], color: Color) extends WrapDrawable {
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
  def drawable: Drawable = {
    val barSpacing = 0
    // barWidth has a meaning in terms of the interpretation of the plot.
    val barWidth = chartAreaSize.width / heightsToDraw.length
    val vScale: Double = chartAreaSize.height / drawYBounds.range
    heightsToDraw.map { h =>
      Style(color)(Scale(y = vScale)(FlipY(drawYBounds.max)(Rect(barWidth, h))))
      //                if (h != 0) Rect(barWidth, h * vScale) filled color
      //                else Style(GreenYellow)(StrokeStyle(Black)(BorderFillRect(barWidth, extent.height)))
    }.seqDistributeH(barSpacing)
  }
}

