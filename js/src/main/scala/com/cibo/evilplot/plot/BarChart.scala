
/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.plot

import com.cibo.evilplot.Style
import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent, FlipY, Rect, Scale}
import com.cibo.evilplot.numeric.Bounds

case class BarChartData(counts: Seq[Double], labels: Seq[String], barWidth: Option[Double] = None,
                        barSpacing: Option[Double] = None) extends PlotData {
  val length: Int = counts.length
  require(length == labels.length, "must be same number of data points as labels")
  override def yBounds: Option[Bounds] = Some(Bounds(if (counts.min > 0) 0 else counts.min, counts.max))
  override def createPlot(extent: Extent, options: PlotOptions): Chart = {
    new BarChart(extent, this, options)
  }
}
/**
  * A categorical bar chart. Each bar corresponds to a different category in the x-variable and is labeled by an
  * entry in label.
  * @param chartSize The size of the bounding box which the chart will occupy.
  * @param data Data object containing counts and labels of each bar.
  * @param options Plot options for the chart.
  */
class BarChart(val chartSize: Extent, data: BarChartData, val options: PlotOptions)
  extends DiscreteX[String] {
  private val numBars = data.length
  val labels: Seq[String] = data.labels
  val defaultYAxisBounds: Bounds = data.yBounds.get // safe because always defined on a BarChartData
  override lazy val xGridLines = EmptyDrawable()

  // Create functions to get width and spacing, depending on what is specified by caller.
  protected val (widthGetter, spacingGetter) = DiscreteChartDistributable
    .widthAndSpacingFunctions(numBars, data.barWidth, data.barSpacing)

    def plottedData(extent: Extent): Drawable = {
      val _barWidth: Double = widthGetter(extent)
      val _barSpacing: Double = spacingGetter(extent)
      val vScale: Double = extent.height / yAxisDescriptor.axisBounds.range
      val bars = data.counts.map { yValue => Style(options.barColor) {
          Scale(y = vScale)(FlipY(yAxisDescriptor.axisBounds.max)(Rect(_barWidth, yValue))) }
      }
      bars.seqDistributeH(_barSpacing) padLeft _barSpacing / 2.0
    }
}
