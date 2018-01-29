package com.cibo.evilplot.plot

import com.cibo.evilplot.colors.{Color, DefaultColors, HTMLNamedColors}
import com.cibo.evilplot.geometry.{Align, Drawable, EmptyDrawable, Extent, Rect}
import com.cibo.evilplot.numeric.{AxisDescriptor, Bounds}
import com.cibo.evilplot.plot.ContinuousChartDistributable._
import com.cibo.evilplot.{Text, Utils}

// TODO: there's a ton of repetition in between these traits, could definitely eliminate w/ some more thought
// (but this repetition eliminates overall repetition in codebase by a lot, so still a good step)

object ContinuousUtilities {
  def maybeGridLines(area: Extent, spacing: Option[Double], desc: AxisDescriptor, color: Color = HTMLNamedColors.white)
                     (glConstructor: (Extent, AxisDescriptor, Double, Color) => GridLines): Drawable =
    spacing match {
      case Some(_spacing) => glConstructor(area, desc, _spacing, color).drawable
      case None => glConstructor(area, desc, desc.spacing, color).drawable
    }
}

// shouldn't have to extend chart
trait ContinuousAxes extends Chart {
  import ContinuousUtilities._
  protected val defaultXAxisBounds: Bounds
  protected val defaultYAxisBounds: Bounds
  private lazy val xAxisDrawBounds: Bounds = options.xAxisBounds.getOrElse(defaultXAxisBounds)
  lazy val xAxisDescriptor: AxisDescriptor = AxisDescriptor(xAxisDrawBounds, options.numXTicks.getOrElse(10))
  private lazy val yAxisDrawBounds: Bounds = options.yAxisBounds.getOrElse(defaultYAxisBounds)
  lazy val yAxisDescriptor: AxisDescriptor = AxisDescriptor(yAxisDrawBounds, options.numYTicks.getOrElse(10))

  // Unideal, but if we're going to be making a plot of a *specific* size, then we have to measure the height and
  // width of the axes, tear these out of the main plot area, then recreate these objects. If we change from a top-down
  // approach we could avoid this?
  // We could also take steps to reduce the amount of recomputation of these axes, if this proves to be too expensive.
  override protected lazy val chartAreaSize: Extent  = {
    val xHeight = XAxis(1, xAxisDescriptor, label = options.xAxisLabel, options.drawXAxis).drawable.extent.height
    val yWidth = YAxis(1, yAxisDescriptor, label = options.yAxisLabel, options.drawYAxis).drawable.extent.width
    chartSize - (w = yWidth, h = xHeight)
  }
  override protected lazy val topLabel: Drawable = Utils.maybeDrawable(options.topLabel)(text =>
    Align.centerSeq(Align.middle(Rect(chartAreaSize.width, 20) filled DefaultColors.titleBarColor, Text(text))).group)

  override protected lazy val rightLabel: Drawable = Utils.maybeDrawable(options.rightLabel)(text =>
    Align.centerSeq(Align.middle(Rect(chartAreaSize.height, 20) filled DefaultColors.titleBarColor,
      Text(text))).group) rotated 90

  override lazy val xAxis: Drawable = XAxis(chartAreaSize.width, xAxisDescriptor, options.xAxisLabel, options.drawXAxis).drawable
  override lazy val yAxis: Drawable = YAxis(chartAreaSize.height, yAxisDescriptor, options.yAxisLabel, options.drawYAxis).drawable
  protected def xGridLines: Drawable = maybeGridLines(chartAreaSize, options.xGridSpacing,
    xAxisDescriptor, options.gridColor)(VerticalGridLines)
  protected def yGridLines: Drawable =
    maybeGridLines(chartAreaSize, options.yGridSpacing, yAxisDescriptor, options.gridColor)(HorizontalGridLines)
  override def chartArea: Drawable = background behind xGridLines behind yGridLines behind plottedData(chartAreaSize)
}
