/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.plot

import com.cibo.evilplot.DOMInitializer
import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent}
import com.cibo.evilplot.numeric.{Bounds, Point}
import com.cibo.evilplot.plotdefs.PlotOptions
import org.scalatest._

import scala.util.Random


// TODO: These tests need a lot of work before they are actually useful!
class ScatterPlotSpec extends FunSpec with Matchers {
  DOMInitializer.init()
  val extent: Extent = Extent(500, 500)
  val gen = Random
  val bounds = Bounds(0, 500)
  val data: Seq[Point] = Seq.fill(1000)(Point(bounds.max * gen.nextDouble(), bounds.max * gen.nextDouble()))
  val options = PlotOptions(xAxisBounds = Some(bounds), yAxisBounds = Some(bounds))
  val plot = new ScatterPlot(extent, data, None, options)
  val (scaleX, scaleY) = (extent.width / bounds.range, extent.height / bounds.range)

  describe("ScatterPlot") {

    ignore("should construct a chart area that is large enough for any point in the axis range to be plotted") {
      val extrema: Seq[Drawable] = Seq(plot.scatterPoint(plot.xAxisDescriptor.minValue, plot.yAxisDescriptor.minValue)(scaleX, scaleY),
        plot.scatterPoint(plot.xAxisDescriptor.minValue, plot.yAxisDescriptor.maxValue)(scaleX, scaleY),
        plot.scatterPoint(plot.xAxisDescriptor.maxValue, plot.yAxisDescriptor.minValue)(scaleX, scaleY),
        plot.scatterPoint(plot.xAxisDescriptor.maxValue, plot.yAxisDescriptor.maxValue)(scaleX, scaleY))

      extrema.foreach { p =>
        p should not be an[EmptyDrawable]
      }
    }
    ignore("should yield an empty drawable when given a point outside the axis bounds") {
      val points: Seq[Drawable] = Seq(plot.scatterPoint(plot.xAxisDescriptor.maxValue + 1, plot.yAxisDescriptor.minValue)(scaleX, scaleY),
        plot.scatterPoint(plot.xAxisDescriptor.minValue, plot.yAxisDescriptor.maxValue + 1)(scaleX, scaleY),
        plot.scatterPoint(plot.xAxisDescriptor.maxValue + 1, plot.yAxisDescriptor.maxValue + 1)(scaleX, scaleY))
      points.foreach { p => p shouldBe an[EmptyDrawable] }
    }

    // Each point has a bounding box that extends from the lowest point in the chart area to just beyond its boundary.
    ignore("should properly place the points in the chart area") {
      data.foreach { case Point(x, y) =>
        val p = plot.scatterPoint(x, y)(scaleX, scaleY)
        p should not be an[EmptyDrawable]
        p.extent.width shouldBe ((x - plot.xAxisDescriptor.axisBounds.min) / plot.xAxisDescriptor.axisBounds.range) *
          plot.extent.width + 2 * plot.pointSize +-   .2
        p.extent.height shouldBe ((plot.yAxisDescriptor.axisBounds.max - y) / plot.yAxisDescriptor.axisBounds.range) *
          plot.extent.height + 2 * plot.pointSize +-   .2
      }
    }
  }

}
