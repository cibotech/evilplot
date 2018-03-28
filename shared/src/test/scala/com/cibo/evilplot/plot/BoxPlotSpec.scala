package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.Extent
import com.cibo.evilplot.numeric.Bounds
import org.scalatest.{FunSpec, Matchers}

class BoxPlotSpec extends FunSpec with Matchers {

  import com.cibo.evilplot.plot.aesthetics.DefaultTheme._

  describe("BoxPlot") {
    it("should have the right extents") {
      val plot = BoxPlot(Seq(Seq(1.0, 2.0)))
      val extent = Extent(100, 200)
      plot.render(extent).extent shouldBe extent
    }

    it("should have the right bounds") {
      val plot = BoxPlot(Seq(Seq(1.0, 2.0)), boundBuffer = Some(0))
      plot.xbounds shouldBe Bounds(0, 0)
      plot.ybounds shouldBe Bounds(1, 2)
    }

    it("should not explode with no data") {
      val plot = BoxPlot(Seq.empty)
      val extent = Extent(200, 200)
      plot.render(extent).extent shouldBe extent
    }

    it("should not explode when there is no data for a box") {
      val plot = BoxPlot(Seq(Seq.empty))
      val extent = Extent(200, 200)
      plot.render(extent).extent shouldBe extent
    }
  }
}
