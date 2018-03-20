package com.cibo.evilplot.plot.components

import com.cibo.evilplot.numeric.{Bounds, Point}
import com.cibo.evilplot.plot.{Bar, BarChart, ScatterPlot}
import org.scalatest.{FunSpec, Matchers}

class AxesSpec extends FunSpec with Matchers {

  import com.cibo.evilplot.plot.aesthetics.DefaultTheme._

  describe("discrete X") {
    it("should set the default bounds") {
      val plot = BarChart(Seq(3.0, 4)).xAxis()
      plot.xbounds shouldBe Bounds(0, 2)
    }

    it("should set bounds for labels") {
      val plot = BarChart(Seq(3.0, 4)).xAxis(Seq("one", "two"))
      plot.xbounds shouldBe Bounds(0, 2)
    }

    it("should set bounds for more labels") {
      val plot = BarChart(Seq(3.0, 4)).xAxis(Seq("one", "two", "three"))
      plot.xbounds shouldBe Bounds(0, 3)
    }

    it("should set bounds for fewer labels") {
      val plot = BarChart(Seq(3.0, 4)).xAxis(Seq("one"))
      plot.xbounds shouldBe Bounds(0, 1)
    }
  }

  describe("continuous X") {
    it("should set reasonable default bounds") {
      val plot = ScatterPlot(Seq(Point(3, 4), Point(5, 6)), boundBuffer = Some(0)).xAxis()
      plot.xbounds shouldBe Bounds(3, 5)
    }

    it("should not update the bounds multiple times") {
      val plot = ScatterPlot(Seq(Point(0, 0), Point(1.007, 2)), boundBuffer = Some(0)).xbounds(0, 1.1).xAxis()
      plot.xbounds.min shouldBe 0.0 +- 1e-6
      plot.xbounds.max shouldBe 1.1 +- 1e-6
    }
  }

  describe("continuous Y") {
    it("should set reasonable default bounds") {
      val plot = ScatterPlot(Seq(Point(3, 4), Point(5, 6)), boundBuffer = Some(0)).yAxis()
      plot.ybounds shouldBe Bounds(4, 6)
    }
  }
}
