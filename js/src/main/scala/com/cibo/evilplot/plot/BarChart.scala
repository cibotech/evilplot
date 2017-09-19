
/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.plot

import com.cibo.evilplot.Style
import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent, FlipY, Rect, Scale}
import com.cibo.evilplot.numeric.Bounds
import com.cibo.evilplot.plotdefs.{BarChartDef, PlotOptions}

/**
  * A categorical bar chart. Each bar corresponds to a different category in the x-variable and is labeled by an
  * entry in label.
  * @param chartSize The size of the bounding box which the chart will occupy.
  * @param data Data object containing counts and labels of each bar.
  * @param options Plot options for the chart.
  */
// TODO: The widthGetter / spacingGetter logic is certainly way too complicated, especially since DrawableLater
// is gone.
class BarChart(val chartSize: Extent, data: BarChartDef, val options: PlotOptions)
  extends DiscreteX {
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
