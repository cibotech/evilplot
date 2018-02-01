/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent}
import com.cibo.evilplot.numeric.{Bounds, Point}
import com.cibo.evilplot.plotdefs.{PlotOptions, ScatterPlotDef, Trendline}
import com.cibo.evilplot.DOMInitializer
import org.scalatest._

import scala.util.Random

// TODO: These tests need a lot of work before they are actually useful!
class ScatterPlotSpec extends FunSpec with Matchers {
  DOMInitializer.init()
  val extent: Extent = Extent(500, 500)
  val gen = Random
  val bounds = Bounds(0, 500)
  val data: Seq[Point] = Seq.fill(1000)(
    Point(bounds.max * gen.nextDouble(), bounds.max * gen.nextDouble()))
  val options =
    PlotOptions(xAxisBounds = Some(bounds), yAxisBounds = Some(bounds))
  val pd = ScatterPlotDef(data, options = options)
  val plot = new ScatterPlot(extent, pd)
  val (scaleX, scaleY) =
    (extent.width / plot.xAxisDescriptor.axisBounds.range, extent.height / plot.yAxisDescriptor.axisBounds.range)

  describe("ScatterPlot") {

    it(
      "should construct a chart area that is large enough for any point in the axis range to be plotted") {
      val extrema: Seq[Drawable] = Seq(
        plot.scatterPoint(plot.xAxisDescriptor.minValue,
                          plot.yAxisDescriptor.minValue)(scaleX, scaleY),
        plot.scatterPoint(plot.xAxisDescriptor.minValue,
                          plot.yAxisDescriptor.maxValue)(scaleX, scaleY),
        plot.scatterPoint(plot.xAxisDescriptor.maxValue,
                          plot.yAxisDescriptor.minValue)(scaleX, scaleY),
        plot.scatterPoint(plot.xAxisDescriptor.maxValue,
                          plot.yAxisDescriptor.maxValue)(scaleX, scaleY)
      )

      extrema.foreach { p =>
        p should not be an[EmptyDrawable]
      }
    }
    it(
      "should yield an empty drawable when given a point outside the axis bounds") {
      val points: Seq[Drawable] = Seq(
        plot.scatterPoint(plot.xAxisDescriptor.maxValue + 1,
                          plot.yAxisDescriptor.minValue)(scaleX, scaleY),
        plot.scatterPoint(plot.xAxisDescriptor.minValue,
                          plot.yAxisDescriptor.maxValue + 1)(scaleX, scaleY),
        plot.scatterPoint(plot.xAxisDescriptor.maxValue + 1,
                          plot.yAxisDescriptor.maxValue + 1)(scaleX, scaleY)
      )
      points.foreach { p =>
        p shouldBe an[EmptyDrawable]
      }
    }

    // Each point has a bounding box that extends from the lowest point in the chart area to just beyond its boundary.
    it("should properly place the points in the chart area") {
      data.foreach {
        case Point(x, y) =>
          val p = plot.scatterPoint(x, y)(scaleX, scaleY)
          p should not be an[EmptyDrawable]
          p.extent.width shouldBe ((x - plot.xAxisDescriptor.axisBounds.min) * scaleX + pd.pointSize) +- .2
          p.extent.height shouldBe ((plot.yAxisDescriptor.axisBounds.max - y) * scaleY + pd.pointSize) +-   .2
      }
    }

  }
  describe("Trend lines") {
    val tolerance = math.ulp(1.0)
    val points = Seq(Point(98, -0.005), Point(112, 0.020))
    val scatter = new ScatterPlot(extent, ScatterPlotDef(points))
    import scatter._
    def getEndpoints(line: Trendline): Option[Seq[Point]] = trendLine(line, xAxisDescriptor.axisBounds,
      yAxisDescriptor.axisBounds)
    it ("should use the x bounds when the x values at extreme ys are outside the plot") {
      val line = Trendline(-0.00015, 0.025)
      val endpoints = getEndpoints(line)
      endpoints shouldBe defined
      val Seq(first, last) = endpoints.get

      first.x shouldBe xAxisDescriptor.axisBounds.min +- tolerance
      last.x shouldBe xAxisDescriptor.axisBounds.max +- tolerance
    }

    it ("doesn't draw anything when the line is not in bounds") {
      val line = Trendline(100, 0.03)
      val endpoints = trendLine(line, xAxisDescriptor.axisBounds, yAxisDescriptor.axisBounds)
      endpoints should not be defined
    }

  }
}
