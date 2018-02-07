package com.cibo.evilplot.plot

import com.cibo.evilplot.numeric.Point
import org.scalatest.{FunSpec, Matchers}

class ScatterPlotSpec extends FunSpec with Matchers {
  describe("ScatterPlot") {
    it("sets reasonable bounds") {
      val data = Seq(Point(-1, 10), Point(20, -5))
      val plot = ScatterPlot(data)

      plot.xbounds.min should be < -1.0
      plot.xbounds.max should be > 20.0
      plot.ybounds.min should be < -5.0
      plot.ybounds.max should be > 10.0
    }

    it("sets exact bounds without buffering") {
      val data = Seq(Point(-1, 10), Point(20, -5))
      val plot = ScatterPlot(data, boundBuffer = 0)

      plot.xbounds.min shouldBe -1.0
      plot.xbounds.max shouldBe 20.0
      plot.ybounds.min shouldBe -5.0
      plot.ybounds.max shouldBe 10.0
    }

    it("sets reasonable bounds with only 1 point") {
      val plot = ScatterPlot(Seq(Point(2, 3)))
      plot.xbounds.min should be < 2.0
      plot.xbounds.max should be > 2.0
      plot.ybounds.min should be < 3.0
      plot.ybounds.max should be > 3.0
    }
  }
}
