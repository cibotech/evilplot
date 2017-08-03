/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.plot

import com.cibo.evilplot.colors._
import com.cibo.evilplot.geometry._
import com.cibo.evilplot.Text
import com.cibo.evilplot.layout.ChartLayout
import com.cibo.evilplot.numeric.Ticks
import org.scalajs.dom.CanvasRenderingContext2D

// Should be able to draw either a histogram with an x-axis that directly labels the bins or
// a histogram that has an extended x-axis and plots the data in that context.

// xBounds: the minimum and maximum x-value of the histogram.
class BarChart(override val extent: Extent, xBounds: Option[Bounds], data: Seq[Double], options: PlotOptions)
  extends Drawable {
  val textAndPadHeight: Int = Text.defaultSize + 5 // text size, stroke width
  private val minValue = 0.0
  private val maxValue = data.reduce[Double](math.max)
  private val xAxisDrawBounds = if (options.xAxisBounds.isDefined) options.xAxisBounds else xBounds
  private val yAxisDrawBounds: Bounds = options.yAxisBounds.getOrElse(Bounds(minValue, maxValue))

  // Fix this .get later. I think it's probably time to just do HistogramChart and BarChart and have them extend
  // some common trait.
  private val xTicks = Ticks(xAxisDrawBounds.get, options.numXTicks.getOrElse(10))
  private val yTicks = Ticks(yAxisDrawBounds, options.numYTicks.getOrElse(10))
  private val bars = Bars(xBounds, xAxisDrawBounds, yAxisDrawBounds, data, options.barColor)
  private val xAxis = XAxis(xTicks)
  private val yAxis = YAxis(yTicks)
  private val chartArea: DrawableLater = {
    def chartArea(extent: Extent): Drawable = {
      val translatedAnnotation = options.annotation match {
        case Some(_annotation) =>
          ((_annotation transX _annotation.position._1 * extent.width)
            transY (_annotation.position._2 * extent.height))
        case None => new EmptyDrawable
      }

      val xGridLines = options.xGridSpacing match {
        case Some(_spacing) => VerticalGridLines(xTicks, _spacing, color = White)(extent)
        case None => EmptyDrawable()
      }

      val yGridLines = options.yGridSpacing match {
        case Some(_spacing) => HorizontalGridLines(yTicks, _spacing, color = White)(extent)
        case None => EmptyDrawable()
      }
      Rect(extent) filled options.backgroundColor behind
        bars(extent) behind xGridLines behind yGridLines behind
        MetricLines(xTicks, Seq(-15, 15), Red)(extent) behind translatedAnnotation
    }
    new DrawableLaterMaker(chartArea)
  }


  val layout: Drawable = ChartLayout(extent, preferredSizeOfCenter = Extent(.85 * extent.width, .85 * extent.height),
    center = chartArea, left = yAxis, bottom = xAxis)

  override def draw(canvas: CanvasRenderingContext2D): Unit = layout.draw(canvas)
}

// This is histogram specific. Histograms might be sufficiently different from categorical charts to not be able
// to reasonably implement these the same way. See:
// https://statistics.laerd.com/statistical-guides/understanding-histograms.php
// Given a (physical) width of a chart, (numeric) width of bins, and number of bins for a histogram, the bin width is
// non-negotiable.
case class Bars(dataXBounds: Option[Bounds],
                drawXBounds: Option[Bounds], drawYBounds: Bounds,
                heights: Seq[Double], color: Color) extends DrawableLater {
  val numBins: Int = heights.length

  lazy val heightsToDraw: Seq[Double] = {
    def trimOrExtendHeights(_left: Double, _right: Double, binWidth: Double): Seq[Double] = {
      val left = math.ceil(_left).toInt
      val right = math.ceil(_right).toInt
      val heightsAdjustedOnLeft = if (left >= 0) Seq.fill[Double](left)(0) ++ heights else heights.drop(-left)
      if (right >= 0) heightsAdjustedOnLeft ++ Seq.fill[Double](right)(0)
      else heightsAdjustedOnLeft.take(heightsAdjustedOnLeft.length + right)
    }

    dataXBounds match {
      case Some(Bounds(dataMin, dataMax)) =>
        val binWidth = (dataMax - dataMin) / numBins
        drawXBounds match {
          case Some(Bounds(drawMin, drawMax)) =>
            // TODO: Incorporate these facts: math.ceil on negative => toward 0, math.floor is opposite.
            // Generate offsets from fractional parts of the number of bins? -> Maybe
            val addOrRemoveBarsOnLeft = (dataMin - drawMin) / binWidth
            val addOrRemoveBarsOnRight = (drawMax - dataMax) / binWidth
            trimOrExtendHeights(addOrRemoveBarsOnLeft, addOrRemoveBarsOnRight, binWidth)
          case None => heights
        }
      case None => heights
    }
  }
  def apply(extent: Extent): Drawable = {
    val barSpacing = 0
    // barWidth has a meaning in terms of the interpretation of the plot.
    val barWidth = extent.width / heightsToDraw.length
    val vScale: Double = extent.height / drawYBounds.max
    val bars: Drawable = Align.bottomSeq {
      heightsToDraw.map { h => Rect(barWidth, h * vScale) filled color
        //        if (h != 0) Rect(barWidth, h * vScale) filled color
        //        else Rect(barWidth, extent.height) filled GreenYellow
      }
    }.seqDistributeH(barSpacing)

    Align.bottom(bars, Rect(1, extent.height) filled Clear).reduce(Beside)
  }
}

