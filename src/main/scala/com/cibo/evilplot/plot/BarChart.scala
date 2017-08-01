/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.plot

import com.cibo.evilplot.colors._
import com.cibo.evilplot.geometry._
import com.cibo.evilplot.Text
import org.scalajs.dom.CanvasRenderingContext2D

// Should be able to draw either a histogram with an x-axis that directly labels the bins or
// a histogram that has an extended x-axis and plots the data in that context.

// xBounds: the minimum and maximum x-value of the histogram.
class BarChart(override val extent: Extent, xBounds: Option[Bounds], data: Seq[Double], options: PlotOptions)
  extends Drawable {
  val textAndPadHeight: Int = Text.defaultSize + 5 // text size, stroke width

  private val barChart = Fit(extent) {
    val minValue = 0.0
    val maxValue = data.reduce[Double](math.max)

    val xAxisDrawBounds: Option[Bounds] = options.xAxisBounds.orElse(xBounds)
    val yAxisDrawBounds = options.yAxisBounds.getOrElse(Bounds(minValue, maxValue))

    val bars = new Bars(extent, xBounds, xAxisDrawBounds, yAxisDrawBounds, data,
      options.barColor, barWidth = options.barWidth)
    // For now assume minValue is zero, we'll need to fix that for graphs that go negative
    // val minValue = data.reduce[Double](math.min)

    val yAxisPadding = 20
    val xAxis = xAxisDrawBounds match {
      case Some(Bounds(minX, maxX)) => Some(new XAxis(bars.extent, minX, maxX, options.numXTicks.getOrElse(4)))
      case None => None
    }

    val yAxis = new YAxis(bars.extent, yAxisDrawBounds.min, yAxisDrawBounds.max, options.numYTicks.getOrElse(4))

    val xMetricLines = options.withinMetrics match {
      case Some(metric) if xAxis.isDefined =>
        new MetricLines(Seq(-metric, metric), xAxis.get, bars, Red)
      case None => new EmptyDrawable
    }

    val translatedAnnotation = options.annotation match {
      case Some(_annotation) =>
        ((_annotation transX _annotation.position._1 * bars.extent.width)
          transY (_annotation.position._2 * bars.extent.height))
      case None => new EmptyDrawable
    }

    val xAxisAndVerticalLines = xAxis match {
      case Some(_xAxis) => options.xGridSpacing match {
        case Some(_xGridSpacing) =>
          val grid = new VerticalGridLines(_xAxis, _xGridSpacing, White)
          _xAxis below grid
        case None => _xAxis
      }
      case None => new EmptyDrawable
    }

    val yAxisAndHorizontalLines = options.yGridSpacing match {
      case Some(_yGridSpacing) =>
        val grid = new HorizontalGridLines(yAxis, _yGridSpacing, White)
        yAxis beside grid
      case None => yAxis
    }

    val chart = (xAxisAndVerticalLines below bars behind xMetricLines) padLeft
      yAxisPadding behind yAxisAndHorizontalLines

    (options.title match {
      case Some(_title) => chart titled(_title, 20) padAll 10
      case None => chart
    }) behind translatedAnnotation
  }
  private val assembledChart = (Rect(barChart.extent.width, barChart.extent.height)
    filled options.backgroundColor) behind barChart

  override def draw(canvas: CanvasRenderingContext2D): Unit = assembledChart.draw(canvas)
}

private[plot] class Bars(chartSize: Extent, dataXBounds: Option[Bounds],
                         drawXBounds: Option[Bounds], drawYBounds: Bounds,
                         heights: Seq[Double], color: Color, barWidth: Option[Double] = None) extends WrapDrawable {
  private val heightsToDraw: Seq[Double] = dataXBounds match {
    case Some(Bounds(dataMin, dataMax)) => drawXBounds match {
      case Some(Bounds(drawMin, drawMax)) =>
        val binWidth = (dataMax - dataMin) / heights.length
        val additionalBarsOnLeft = math.ceil((dataMin - drawMin) / binWidth).toInt
        val additionalBarsOnRight = math.ceil((drawMax - dataMax) / binWidth).toInt
        Seq.fill[Double](additionalBarsOnLeft)(0) ++ heights ++ Seq.fill[Double](additionalBarsOnRight)(0)
      case None => heights
    }
    case None => heights
  }
  val nBars: Int = heightsToDraw.length
  val _barWidth: Double = barWidth match {
    case Some(x) => x
    case None => chartSize.width / nBars
  }
  val barSpacing = 0
  val vScale: Double = chartSize.height / drawYBounds.max
  val bars: Drawable = Align.bottomSeq {
    val rects = heightsToDraw.map { h => Rect(_barWidth, h * vScale) }
    rects.map {
      rect => rect filled color
    }
  }.seqDistributeH(barSpacing)

  override def drawable: Drawable = Align.bottom(bars, Rect(1, chartSize.height) filled Clear).reduce(Beside)
}

