
/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.plot

import com.cibo.evilplot.Style
import com.cibo.evilplot.geometry._
import com.cibo.evilplot.layout.ChartLayout
import com.cibo.evilplot.numeric.AxisDescriptor
import org.scalajs.dom.CanvasRenderingContext2D
/**
  * A categorical bar chart. Each bar corresponds to a different category in the x-variable and is labeled by an
  * entry in label.
  * @param extent The size of the bounding box which the chart will occupy.
  * @param labels List of labels for bars in the order they are to be plotted.
  * @param data List of y-values; each entry corresponds to an entry in `labels`. It must have the same length
  *             as `labels`
  * @param options Plot options for the chart.
  * @param barWidth width of bars in pixels. automatically calculated if not provided.
  * @param barSpacing padding in between bars in pixels. automatically calculated if not provided. Ignored if `barWidth`
  *                   is specified.
  */
class BarChart(override val extent: Extent, labels: Seq[String], data: Seq[Double], options: PlotOptions,
               barWidth: Option[Double] = None, barSpacing: Option[Double] = None) extends Drawable {
  require(labels.length == data.length, "Labels and data must have the same length.")
  private val numBars = labels.length
  private val minValue = 0.0
  private val maxValue = data.reduce[Double](math.max)
  private val yAxisDrawBounds: Bounds = options.yAxisBounds.getOrElse(Bounds(minValue, maxValue))
  private val yAxisDescriptor = AxisDescriptor(yAxisDrawBounds, options.numYTicks.getOrElse(10))

  // Create functions to get width and spacing, depending on what is specified by caller.
  private val (getBarWidth, getBarSpacing) = DiscreteChartDistributable
    .widthAndSpacingFunctions(numBars, barWidth, barSpacing)

  private val _drawable = {
    val xAxis = DiscreteChartDistributable.XAxis(labels, getBarWidth, getBarSpacing, options.xAxisLabel,
      rotateText = 90)
    val yAxis = ContinuousChartDistributable.YAxis(yAxisDescriptor, label = options.yAxisLabel)

    def chartArea(extent: Extent): Drawable = {
      val _barWidth: Double = getBarWidth(extent)
      val _barSpacing: Double = getBarSpacing(extent)
      val vScale: Double = extent.height / yAxisDescriptor.axisBounds.range
      val bars: Drawable = data.map { yValue => Style(options.barColor) {
          Scale(y = vScale)(FlipY(yAxisDescriptor.axisBounds.max)(Rect(_barWidth, yValue))) }
      }.seqDistributeH(_barSpacing) padLeft _barSpacing / 2.0
      val xGridLines = DiscreteChartDistributable.VerticalGridLines(numBars, getBarWidth, getBarSpacing)(extent)
      val yGridLines = ContinuousChartDistributable
        .HorizontalGridLines(yAxisDescriptor, options.yGridSpacing.getOrElse(10))(extent)
      Rect(extent) filled options.backgroundColor behind xGridLines behind yGridLines behind bars titled(
        options.title.getOrElse(""), 20.0)
    }
    val centerProportion: Double = 0.90
    new ChartLayout(extent = extent, preferredSizeOfCenter = extent * centerProportion,
      center = new DrawableLaterMaker(chartArea), left = yAxis, bottom = xAxis)
  }
  override def draw(canvas: CanvasRenderingContext2D): Unit = _drawable.draw(canvas)
}
