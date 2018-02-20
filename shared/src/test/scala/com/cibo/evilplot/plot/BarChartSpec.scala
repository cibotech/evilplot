package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.Extent
import com.cibo.evilplot.numeric.Bounds
import org.scalatest.{FunSpec, Matchers}

class BarChartSpec extends FunSpec with Matchers {
  describe("BarChart") {
    it("should have the right bounds without buffer") {
      val plot = BarChart(Seq(Bar(10), Bar(20), Bar(15)), boundBuffer = 0)
      plot.xbounds shouldBe Bounds(0, 3)
      plot.ybounds shouldBe Bounds(10, 20)
    }

    it("should have the right bounds with buffer") {
      val plot = BarChart(Seq(Bar(10), Bar(20), Bar(15)))
      plot.xbounds shouldBe Bounds(0, 3)
      plot.ybounds.min should be < 10.0
      plot.ybounds.max should be > 20.0
    }

    it("should have the right bounds with stacked bars") {
      val plot = BarChart(Seq(Bar(Seq(10.0, 5)), Bar(Seq(20.0, 7)), Bar(Seq(15.0, 0))), boundBuffer = 0)
      plot.xbounds shouldBe Bounds(0, 3)
      plot.ybounds shouldBe Bounds(15, 27)
    }

    it("should have the right extents") {
      val plot = BarChart(Seq(Bar(10), Bar(20), Bar(15)))
      val extent = Extent(200, 200)
      plot.render(extent).extent shouldBe extent
    }
  }
}
