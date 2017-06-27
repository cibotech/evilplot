/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.{Align, Beside, Drawable, Extent, Fit, Line, Rect, WrapDrawable}
import com.cibo.evilplot.numeric.Ticks
import com.cibo.evilplot.{Text, colors}
import org.scalajs.dom.CanvasRenderingContext2D


class BarChart(override val extent: Extent, data: Seq[Double], title: Option[String] = None)
  extends Drawable {
  val textAndPadHeight = Text.defaultSize + 5 // text size, stroke width

  private val chart = Fit(extent) {
    val bars = new Bars(data)
    // For now assume minValue is zero, we'll need to fix that for graphs that go negative
    //val minValue = data.reduce[Double](math.min)
    val minValue = 0.0
    val maxValue = data.reduce[Double](math.max)
    val _yAxis = new YAxis(bars.extent.height, minValue, maxValue)
    _yAxis beside bars
  }
  val barChart = title match {
    case Some(_title) => chart titled (_title, 20) padAll 10
    case None => chart
  }

  override def draw(canvas: CanvasRenderingContext2D): Unit = barChart.draw(canvas)

  private class Bars(heights: Seq[Double]) extends WrapDrawable {
    val barWidth = 50
    val barSpacing = 6
    val bars: Drawable = Align.bottomSeq {
      val rects = heights.map { h => Rect(barWidth, h) titled h.toString}
      rects.map {
        rect => rect filled colors.Black //labeled color.repr
      }
    }.seqDistributeH(barSpacing)

    override def drawable: Drawable = bars
  }

  private class YAxis(height: Double, minValue: Double, maxValue: Double) extends WrapDrawable {
    private val (tickMin, tickMax, spacing, numFrac) = Ticks.niceTicks(minValue, maxValue, 10)
    private val numTicks: Int = math.round((tickMax - tickMin) / spacing).toInt + 1
    private val ticks = {
      val tickThick = 1
      val tickLength = 5
      (for {
      // The graphics origin is in the top left, so we are walking the ticks in reverse order
        numTick <- (numTicks - 1) to 0 by -1
        label = (tickMin + (numTick * spacing)).toString
        tick = new Tick(tickLength, tickThick, Some(label))
        tickHeight = tick.extent.height
        padTop = if (numTick == numTicks - 1) height - tickMax - (tickHeight / 2.0) else spacing - tickHeight
      } yield {
        tick.padTop(padTop)
      }).seqDistributeV
    }

    override def drawable: Drawable = ticks
  }

  private class Tick(length: Double, thickness: Double, label: Option[String] = None) extends WrapDrawable {
    private val line = Line(length, thickness)

    override def drawable: Drawable = label match {
      // TODO: fix the padding fudge factors
      case Some(_label) => Align.middle(Text(_label).padRight(2).padBottom(2), line).reduce(Beside)
      case None => line
    }
  }
}
