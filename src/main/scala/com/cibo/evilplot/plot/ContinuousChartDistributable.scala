package com.cibo.evilplot.plot

import com.cibo.evilplot.colors.{Color, White}
import com.cibo.evilplot.geometry.{Above, Align, Beside, Drawable, DrawableLater, EmptyDrawable, Extent, Line}
import com.cibo.evilplot.numeric.Ticks
import com.cibo.evilplot.{Text, Utils}

object ContinuousChartDistributable {
  /* Base trait for axes and grid lines. */
  trait ContinuousChartDistributableBase extends DrawableLater {
    private[plot] val ticks: Ticks
    protected val tickThick = 1
    protected val tickLength = 5
    private[plot] lazy val bounds: Bounds = ticks.bounds
    private[plot] def pixelsPerUnit(distributableDimension: Double): Double = distributableDimension / bounds.range


    def getLinePosition(coord: Double, distributableDimension: Double): Double =
      (coord - bounds.min) * pixelsPerUnit(distributableDimension)

  }

  case class XAxis(ticks: Ticks, label: Option[String] = None, drawTicks: Boolean = true)
    extends ContinuousChartDistributableBase {
    def apply(extent: Extent): Drawable = {
      lazy val text = Utils.maybeDrawable(label, (msg: String) => Text(msg, 22))
      val _ticks = (for {
        numTick <- 0 until ticks.numTicks
        coordToDraw = ticks.tickMin + numTick * ticks.spacing
        label = Utils.createNumericLabel(coordToDraw, ticks.numFrac)
        tick = new VerticalTick(tickLength, tickThick, Some(label))

        padLeft = getLinePosition(coordToDraw, extent.width) - tick.extent.width / 2.0
      } yield tick padLeft padLeft).group
      lazy val _drawable = Align.center(_ticks, text).reduce(Above)
      if (drawTicks) _drawable else EmptyDrawable()
    }
  }

  case class YAxis(ticks: Ticks, label: Option[String] = None, drawTicks: Boolean = true)
    extends ContinuousChartDistributableBase {
    def apply(extent: Extent): Drawable = {
      lazy val text = Utils.maybeDrawable(label, (msg: String) => Text(msg, 20) rotated 270)
      val _ticks = for {
        numTick <- (ticks.numTicks - 1) to 0 by -1
        coordToDraw = ticks.tickMin + numTick * ticks.spacing
        label = Utils.createNumericLabel(coordToDraw, ticks.numFrac)
        tick = new HorizontalTick(tickLength, tickThick, Some(label))

        padTop = extent.height - getLinePosition(coordToDraw, extent.height) - tick.extent.height / 2.0
      } yield tick padTop padTop

      lazy val _drawable = Align.middle(text padRight 10, Align.rightSeq(_ticks).group).reduce(Beside)
      if (drawTicks) _drawable else EmptyDrawable()
    }
  }


  trait GridLines extends ContinuousChartDistributableBase {
    val lineSpacing: Double
    private[plot] val nLines: Int = math.ceil(ticks.bounds.range / lineSpacing).toInt

    // Calculate the coordinate of the first grid line to be drawn.
    private val maxNumLines = math.ceil((ticks.tickMin - ticks.bounds.min) / lineSpacing).toInt
    protected val minGridLineCoord: Double = {
      if (maxNumLines == 0) ticks.bounds.min
      else List.tabulate[Double](maxNumLines)(ticks.tickMin - _ * lineSpacing).filter(_ >= ticks.bounds.min).min
    }
  }

  case class VerticalGridLines(ticks: Ticks, lineSpacing: Double, color: Color = White) extends GridLines {
    def apply(extent: Extent): Drawable = {
      require(nLines != 0)
      val lines = for {
        nLine <- 0 until nLines
        line = Line(extent.height, 1) rotated 90 colored color
        lineWidthCorrection = line.extent.width / 2.0
        padding = getLinePosition(minGridLineCoord + nLine * lineSpacing, extent.width) - lineWidthCorrection
      } yield {
        line padLeft padding
      }
      lines.group
    }
  }

  case class HorizontalGridLines(ticks: Ticks, lineSpacing: Double, color: Color = White) extends GridLines {
    def apply(extent: Extent): Drawable = {
      require(nLines != 0)
      val lines = for {
        nLines <- (nLines - 1) to 0 by -1
        line = Line(extent.width, 1) colored color
        lineCorrection = line.extent.height / 2.0
        padding = extent.height - getLinePosition(minGridLineCoord + nLines * lineSpacing, extent.height) -
          lineCorrection
      } yield line padTop padding
      lines.group
    }
  }

  // TODO: Labeling these vertical lines in a way that doesn't mess up their positioning!
  case class MetricLines(ticks: Ticks, linesToDraw: Seq[Double], color: Color)
    extends ContinuousChartDistributableBase {
    def apply(extent: Extent): Drawable = {
      val lines = for {
        line <- linesToDraw
        padLeft = (line - ticks.bounds.min) * pixelsPerUnit(extent.width)
      } yield Line(extent.height, 2) colored color rotated 90 padLeft padLeft
      lines.group
    }
  }
}
