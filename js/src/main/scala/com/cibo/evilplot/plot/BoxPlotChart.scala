package com.cibo.evilplot.plot

import com.cibo.evilplot.colors.Color
import com.cibo.evilplot.colors.HTMLNamedColors.{blue, white}
import com.cibo.evilplot.geometry._
import com.cibo.evilplot.numeric.{Bounds, BoxPlot}
import com.cibo.evilplot.plotdefs._
import com.cibo.evilplot.{StrokeStyle, Style}

// TODO: ggplot2 provides a `geom_jitter` which makes the outliers a bit easier to read off the plot.
// TODO: Continuous x option?

class BoxPlotChart(val chartSize: Extent, data: BoxPlotDef, val options: PlotOptions)
  extends DiscreteX {
  val defaultYAxisBounds: Bounds = data.yBounds.get // guaranteed to be defined.
  val labels: Seq[String] = data.labels
  protected val (widthGetter, spacingGetter) = DiscreteChartDistributable
    .widthAndSpacingFunctions(data.numBoxes, data.rectWidth, data.rectSpacing)

  // TODO bring this out too.
  private def createDiscs(pointsData: Seq[Double], vScale: Double): Drawable = {
    val points = for {
      point <- pointsData
    } yield Disc(data.pointSize, 0, (point - yAxisDescriptor.axisBounds.min) * vScale)
    FlipY(points.group) transY ((yAxisDescriptor.axisBounds.max - pointsData.max) * vScale - data.pointSize)
  }
  /*    val topLabel = Utils.maybeDrawableLater(options.topLabel, (text: String) => Label(text))
      val rightLabel = Utils.maybeDrawableLater(options.rightLabel, (text: String) => Label(text, rotate = 90))*/
  def plottedData(extent: Extent): Drawable = {
    val _rectWidth = widthGetter(extent)
    val _rectSpacing = spacingGetter(extent)
    val vScale = extent.height / yAxisDescriptor.axisBounds.range
    val boxes = for {
      distribution <- data.distributions
      boxPlot = new BoxPlot(distribution)
      box = new Box(yAxisDescriptor.axisBounds, _rectWidth, vScale, boxPlot)
      discs = data.drawPoints match {
        case AllPoints => createDiscs(distribution, vScale)
        case OutliersOnly => createDiscs(boxPlot.outliers, vScale)
        case NoPoints => EmptyDrawable()
      }
    } yield Align.center(box, discs).group
    boxes.seqDistributeH(_rectSpacing) padLeft _rectSpacing / 2.0
  }
}

private class Box(yBounds: Bounds, rectWidth: Double, vScale: Double, data: BoxPlot, strokeColor: Color = blue)
  extends WrapDrawable {
  private val _drawable = {
    val rectangles = {
      val lowerRectangleHeight: Double = (data.middleQuantile - data.lowerQuantile) * vScale
      val upperRectangleHeight: Double = (data.upperQuantile - data.middleQuantile) * vScale
      StrokeStyle(strokeColor)(Style(white)
      (BorderFillRect(rectWidth, lowerRectangleHeight) below BorderFillRect(rectWidth, upperRectangleHeight)))
    }
    val upperWhisker = Line((data.upperWhisker - data.upperQuantile) * vScale, 2) rotated 90
    val lowerWhisker = Line((data.lowerQuantile - data.lowerWhisker) * vScale, 2) rotated 90
    val nudgeBoxY = (yBounds.max - data.upperWhisker) * vScale
    StrokeStyle(strokeColor)(Align.center(upperWhisker, rectangles, lowerWhisker).reduce(Above)) transY nudgeBoxY
  }
  override val drawable: Drawable = _drawable
}
