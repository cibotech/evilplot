package com.cibo.evilplot.oldplot

import com.cibo.evilplot.colors.Color
import com.cibo.evilplot.colors.HTMLNamedColors.white
import com.cibo.evilplot.geometry._
import com.cibo.evilplot.numeric.{AxisDescriptor, Bounds}

// TODO: ChartDistributable is not really a useful abstraction. Most of the code is the same.
object ContinuousChartDistributable {
  /* Base trait for axes and grid lines. */
  trait ContinuousChartDistributableBase {
    private[oldplot] val axisDescriptor: AxisDescriptor
    protected val distributableDimension: Double
    protected val tickThick = 1
    protected val tickLength = 5
    private[oldplot] lazy val bounds: Bounds = axisDescriptor.axisBounds
    private[oldplot] lazy val pixelsPerUnit: Double = distributableDimension / bounds.range

    def getLinePosition(coord: Double, distributableDimension: Double): Double =
      (coord - bounds.min) * pixelsPerUnit

  }

  case class XAxis(distributableDimension: Double, axisDescriptor: AxisDescriptor,
                   label: Option[String] = None, drawTicks: Boolean = true) extends ContinuousChartDistributableBase {
      lazy val text = label.map(msg => Text(msg, 22)).getOrElse(EmptyDrawable())
      private val _ticks = for {
        numTick <- 0 until axisDescriptor.numTicks
        coordToDraw = axisDescriptor.axisBounds.min + numTick * axisDescriptor.spacing
        label = Chart.createNumericLabel(coordToDraw, axisDescriptor.numFrac)
        tick = VerticalTick(tickLength, tickThick, Some(label)).drawable

        padLeft = getLinePosition(coordToDraw, distributableDimension) - tick.extent.width / 2.0
      } yield tick padLeft padLeft
      lazy val _drawable = Align.center(_ticks.group, text).reduce(above)
      def drawable: Drawable = if (drawTicks) _drawable padTop 2 else EmptyDrawable()
  }

  case class YAxis(distributableDimension: Double, axisDescriptor: AxisDescriptor,
                   label: Option[String] = None, drawTicks: Boolean = true) extends ContinuousChartDistributableBase {
      private lazy val text = label.map(msg => Text(msg, 20) rotated 270).getOrElse(EmptyDrawable())
      private val _ticks = for {
        numTick <- (axisDescriptor.numTicks - 1) to 0 by -1
        coordToDraw = axisDescriptor.tickMin + numTick * axisDescriptor.spacing
        label = Chart.createNumericLabel(coordToDraw, axisDescriptor.numFrac)
        tick = HorizontalTick(tickLength, tickThick, Some(label)).drawable

        padTop = distributableDimension - getLinePosition(coordToDraw, distributableDimension) - tick.extent.height / 2.0
      } yield tick padTop padTop

      private lazy val _drawable = Align.middle(text padRight 10, Align.rightSeq(_ticks).group).reduce(beside)
      def drawable: Drawable = if (drawTicks) _drawable padRight 2 else EmptyDrawable()
  }


  trait GridLines extends ContinuousChartDistributableBase {
    val lineSpacing: Double
    private[oldplot] val nLines: Int = math.ceil(bounds.range / lineSpacing).toInt
    protected val chartAreaSize: Extent // size of area in which to draw the grid lines.
    protected val minGridLineCoord: Double = axisDescriptor.tickMin
    def drawable: Drawable
  }

  case class VerticalGridLines(
    chartAreaSize: Extent,
    axisDescriptor: AxisDescriptor,
    lineSpacing: Double,
    color: Color = white
  ) extends GridLines {
    protected val distributableDimension: Double = chartAreaSize.width
    private val lines = for {
      nLine <- 0 until nLines
      line = Line(chartAreaSize.height, 1) rotated 90 colored color
      lineWidthCorrection = line.extent.width / 2.0
      padding = getLinePosition(minGridLineCoord + nLine * lineSpacing, chartAreaSize.height) - lineWidthCorrection
    } yield { line padLeft padding }

    def drawable: Drawable = lines.group
  }

  case class HorizontalGridLines(chartAreaSize: Extent, axisDescriptor: AxisDescriptor,
                                 lineSpacing: Double, color: Color = white) extends GridLines {
      protected val distributableDimension: Double = chartAreaSize.height
      private val lines = for {
        nLines <- (nLines - 1) to 0 by -1
        line = Line(chartAreaSize.width, 1) colored color
        lineCorrection = line.extent.height / 2.0
        padding = chartAreaSize.height - getLinePosition(minGridLineCoord + nLines * lineSpacing, chartAreaSize.width) -
          lineCorrection
      } yield line padTop padding
      def drawable: Drawable = lines.group
  }

  // TODO: Labeling these vertical lines in a way that doesn't mess up their positioning!
  case class VLines(chartAreaSize: Extent, axisDescriptor: AxisDescriptor,
                    linesToDraw: Seq[(Double, Color)])
    extends ContinuousChartDistributableBase {
    val distributableDimension: Double = chartAreaSize.width
      val lines: Seq[Drawable] = for {
        (line, color) <- linesToDraw
        padLeft = (line - bounds.min) * pixelsPerUnit
      } yield Line(chartAreaSize.height, 2) colored color rotated 90 padLeft padLeft
      def drawable: Drawable = lines.group
  }

  case class HLines(
    chartAreaSize: Extent,
    axisDescriptor: AxisDescriptor,
    linesToDraw: Seq[(Double, Color)]
  ) extends ContinuousChartDistributableBase {
    val distributableDimension: Double = chartAreaSize.height
    val lines: Seq[Drawable] = for {
      (line, color) <- linesToDraw
      padTop = (bounds.max - line) * pixelsPerUnit
    } yield Line(chartAreaSize.width, 2) colored color padTop padTop

    def drawable: Drawable = lines.group
  }
}
