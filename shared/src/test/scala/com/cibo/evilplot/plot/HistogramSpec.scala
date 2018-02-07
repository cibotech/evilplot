package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.Extent
import com.cibo.evilplot.numeric.Bounds
import org.scalatest.{FunSpec, Matchers}

class HistogramSpec extends FunSpec with Matchers {
  describe("Histogram") {
    val plot = Histogram(Seq(1.0, 1, 1, 2, 3, 4, 4, 5), boundBuffer = 0)

    it("has the right bounds") {
      plot.xbounds shouldBe Bounds(1, 5)
      plot.ybounds shouldBe Bounds(0, 3)
    }

    it("has the right extents") {
      val extent = Extent(300, 400)
      plot.render(extent).extent.width shouldBe extent.width +- 1e-6
      plot.render(extent).extent.height shouldBe extent.height +- 1e-6
    }
  }
}
