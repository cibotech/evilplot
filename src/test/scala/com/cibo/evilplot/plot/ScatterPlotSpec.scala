/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.plot

import com.cibo.evilplot.DOMInitializer
import com.cibo.evilplot.colors.HSL
import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent, Point}
import com.cibo.evilplot.numeric._
import org.scalatest._

import scala.util.Random


// TODO: These tests need a lot of work before they are actually useful!
class ScatterPlotSpec extends FunSpec with Matchers {

  DOMInitializer.init()
  val extent: Extent = Extent(500, 500)
  val gen = Random
  val b = Bounds(0, 500)
  val data: Seq[Point] = Seq.fill(1000)(Point(b.max * gen.nextDouble(), b.max * gen.nextDouble()))
  val options = PlotOptions(xAxisBounds = Some(b), yAxisBounds = Some(b))
  val plot = new ScatterPlot(extent, data, None, options)

  describe("ScatterPlot") {
    it("should construct a chart area that is large enough for any point in the axis range to be plotted") {
      val extrema: Seq[Drawable] = Seq(plot.scatterPoint(plot.xAxis.minValue, plot.yAxis.minValue),
        plot.scatterPoint(plot.xAxis.minValue, plot.yAxis.maxValue),
        plot.scatterPoint(plot.xAxis.maxValue, plot.yAxis.minValue),
        plot.scatterPoint(plot.xAxis.maxValue, plot.yAxis.maxValue))

      extrema.foreach { p =>
        p should not be an[EmptyDrawable]
      }
    }
    it("should yield an empty drawable when given a point outside the axis bounds") {
      val points: Seq[Drawable] = Seq(plot.scatterPoint(plot.xAxis.maxValue + 1, plot.yAxis.minValue),
        plot.scatterPoint(plot.xAxis.minValue, plot.yAxis.maxValue + 1),
        plot.scatterPoint(plot.xAxis.maxValue + 1, plot.yAxis.maxValue + 1))
      points.foreach { p => p shouldBe an[EmptyDrawable] }
    }

    // Each point has a bounding box that extends from the lowest point in the chart area to just beyond its boundary.
    it("should properly place the points in the chart area") {
      data.foreach { case Point(x, y) =>
        val p = plot.scatterPoint(x, y)
        p should not be an[EmptyDrawable]
        p.extent.width shouldBe ((x - plot.xAxisBounds.min) / plot.xAxisBounds.range) *
          plot.extent.width + 2 * plot.pointSize +-   .2
        // 3 * plot.pointSize because of the transY pointSize
        p.extent.height shouldBe ((y - plot.yAxisBounds.min) / plot.yAxisBounds.range) *
          plot.extent.height + 3 * plot.pointSize +-   .2
      }
    }
  }

}
