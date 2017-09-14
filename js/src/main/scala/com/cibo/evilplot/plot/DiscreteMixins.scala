package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.{Drawable, Extent}
import com.cibo.evilplot.numeric.Bounds
import com.cibo.evilplot.plot.DiscreteChartDistributable._

// trying to accomplish this writing as little code as possible...
trait DiscreteX[T] extends ContinuousAxes {
  val labels: Seq[T]
  protected val widthGetter: (Extent => Double)
  protected val spacingGetter: (Extent => Double)
  override lazy val defaultXAxisBounds = Bounds(0, 1) // silly, shouldn't be used.

  // TODO: right now rotate text is set to 90 for possibility of long labels, should be incorporating our knowledge
  // of label length and axis creation should be robust to the possibility of ridiculous axis labels
  override lazy val xAxis = XAxis(chartAreaSize, labels, widthGetter, spacingGetter, options.xAxisLabel,
    rotateText = 90, drawAxis = options.drawXAxis)
  override lazy val xGridLines: Drawable = VerticalGridLines(chartAreaSize, labels.length, widthGetter, spacingGetter)
}

