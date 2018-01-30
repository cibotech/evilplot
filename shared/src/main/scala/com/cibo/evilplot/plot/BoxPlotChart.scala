package com.cibo.evilplot.plot

import com.cibo.evilplot.colors.Color
import com.cibo.evilplot.colors.HTMLNamedColors.{blue, white}
import com.cibo.evilplot.geometry._
import com.cibo.evilplot.numeric.{Bounds, BoxPlotSummaryStatistics}
import com.cibo.evilplot.plotdefs._

// TODO: ggplot2 provides a `geom_jitter` which makes the outliers a bit easier to read off the plot.
// TODO: Continuous x option?

case class BoxPlotChart(chartSize: Extent, data: BoxPlotDef) extends DiscreteX {
  val options: PlotOptions = data.options
  val defaultYAxisBounds: Bounds = data.yBounds.get // guaranteed to be defined.
  val labels: Seq[String] = data.labels
  protected val (widthGetter, spacingGetter) = DiscreteChartDistributable
    .widthAndSpacingFunctions(data.numBoxes, data.rectWidth, data.rectSpacing)

  // TODO bring this out too.
  private def createDiscs(pointsData: Seq[Double], vScale: Double): Drawable = {
    val points = for {
      point <- pointsData
    } yield Disc(data.pointSize, 0, (point - yAxisDescriptor.axisBounds.min) * vScale)
    flipY(points.group) transY ((yAxisDescriptor.axisBounds.max - pointsData.max) * vScale - data.pointSize) transX
      data.pointSize
  }
  def plottedData(extent: Extent): Drawable = {
    val _rectWidth = widthGetter(extent)
    val _rectSpacing = spacingGetter(extent)
    val vScale = extent.height / yAxisDescriptor.axisBounds.range
    val boxes = for {
      summary <- data.summaries
      box = Box(yAxisDescriptor.axisBounds, _rectWidth, vScale, summary).drawable
      discs = data.drawPoints match {
        case OutliersOnly if summary.outliers.nonEmpty => createDiscs(summary.outliers, vScale)
        case AllPoints if summary.allPoints.nonEmpty => createDiscs(summary.allPoints, vScale)
        case _ => EmptyDrawable()
      }
    } yield Align.center(box, discs).group
    boxes.seqDistributeH(_rectSpacing) padLeft _rectSpacing / 2.0
  }
}

private case class Box(
  yBounds: Bounds,
  rectWidth: Double,
  vScale: Double,
  data: BoxPlotSummaryStatistics,
  strokeColor: Color = blue
) {
  private val _drawable = {
    val rectangles = {
      val lowerRectangleHeight: Double = (data.middleQuantile - data.lowerQuantile) * vScale
      val upperRectangleHeight: Double = (data.upperQuantile - data.middleQuantile) * vScale
      StrokeStyle(
        Style(
          BorderRect.filled(rectWidth, lowerRectangleHeight) below BorderRect.filled(rectWidth, upperRectangleHeight),
          white
        ),
        strokeColor
      )
    }
    val upperWhisker = Line((data.upperWhisker - data.upperQuantile) * vScale, 2) rotated 90
    val lowerWhisker = Line((data.lowerQuantile - data.lowerWhisker) * vScale, 2) rotated 90
    val nudgeBoxY = (yBounds.max - data.upperWhisker) * vScale
    StrokeStyle(Align.center(upperWhisker, rectangles, lowerWhisker).reduce(Above.apply), strokeColor) transY nudgeBoxY
  }
  val drawable: Drawable = _drawable
}
