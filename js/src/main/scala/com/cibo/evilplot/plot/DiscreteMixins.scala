package com.cibo.evilplot.plot

import com.cibo.evilplot.{Text, Utils}
import com.cibo.evilplot.colors.{DefaultColors, HSL, HTMLNamedColors}
import com.cibo.evilplot.geometry.{Align, Drawable, Extent, Rect}
import com.cibo.evilplot.numeric.Bounds
import com.cibo.evilplot.plot.ContinuousChartDistributable.YAxis
import com.cibo.evilplot.plot.DiscreteChartDistributable._

// trying to accomplish this writing as little code as possible...
trait DiscreteX extends ContinuousAxes {
  val labels: Seq[String]
  protected val widthGetter: (Extent => Double)
  protected val spacingGetter: (Extent => Double)
  override lazy val defaultXAxisBounds = Bounds(0, 1) // silly, shouldn't be used.
  override protected lazy val topLabel: Drawable = Utils.maybeDrawable(options.topLabel)(text =>
    Align.centerSeq(Align.middle(Rect(chartAreaSize.width, 20) filled DefaultColors.titleBarColor, Text(text))).group)

  override protected lazy val rightLabel: Drawable = Utils.maybeDrawable(options.rightLabel)(text =>
    Align.centerSeq(Align.middle(Rect(chartAreaSize.height, 20) filled DefaultColors.titleBarColor,
      Text(text))).group) rotated 90
  override protected lazy val chartAreaSize: Extent  = {
    val xHeight = XAxis(Extent(chartSize.width, 1), labels, widthGetter, spacingGetter, label = options.xAxisLabel,
      rotateText = 90, options.drawXAxis).drawable.extent.height
    val yWidth = YAxis(1, yAxisDescriptor, label = options.yAxisLabel, options.drawYAxis).drawable.extent.width
    chartSize - (w = yWidth, h = xHeight)
  }

  // TODO: right now rotate text is set to 90 for possibility of long labels, should be incorporating our knowledge
  // of label length and axis creation should be robust to the possibility of ridiculous axis labels
  override lazy val xGridLines: Drawable = VerticalGridLines(chartAreaSize, labels.length, widthGetter, spacingGetter).drawable
  override lazy val xAxis: Drawable = XAxis(
    chartAreaSize, labels, widthGetter, spacingGetter, options.xAxisLabel, rotateText = 90,
    drawAxis = options.drawXAxis
  ).drawable
}

