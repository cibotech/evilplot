package com.cibo.evilplot.plot

import com.cibo.evilplot.{Text, Utils}
import com.cibo.evilplot.colors.HSL
import com.cibo.evilplot.geometry.{Align, Drawable, Extent, Rect}
import com.cibo.evilplot.numeric.Bounds
import com.cibo.evilplot.plot.DiscreteChartDistributable._

// trying to accomplish this writing as little code as possible...
trait DiscreteX extends ContinuousAxes {
  val labels: Seq[String]
  protected val widthGetter: (Extent => Double)
  protected val spacingGetter: (Extent => Double)
  override lazy val defaultXAxisBounds = Bounds(0, 1) // silly, shouldn't be used.
  override protected lazy val topLabel: Drawable = Utils.maybeDrawable(options.topLabel)(text =>
    Align.centerSeq(Align.middle(Rect(chartAreaSize.width, 20) filled HSL(0, 0, 85), Text(text))).group)

  override protected lazy val rightLabel: Drawable = Utils.maybeDrawable(options.rightLabel)(text =>
    Align.centerSeq(Align.middle(Rect(chartAreaSize.height, 20) filled HSL(0, 0, 85), Text(text))).group) rotated 90

  // TODO: right now rotate text is set to 90 for possibility of long labels, should be incorporating our knowledge
  // of label length and axis creation should be robust to the possibility of ridiculous axis labels
  override lazy val xAxis = XAxis(chartAreaSize, labels, widthGetter, spacingGetter, options.xAxisLabel,
    rotateText = 90, drawAxis = options.drawXAxis)
  override lazy val xGridLines: Drawable = VerticalGridLines(chartAreaSize, labels.length, widthGetter, spacingGetter)
}

