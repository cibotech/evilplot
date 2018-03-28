package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.Extent
import com.cibo.evilplot.numeric.Bounds
import org.scalatest.{FunSpec, Matchers}

class HeatmapSpec extends FunSpec with Matchers {

  import com.cibo.evilplot.plot.aesthetics.DefaultTheme._

  describe("Heatmap") {
    it("has the right bounds") {
      val plot = Heatmap(Seq(Seq(1), Seq(2)))
      plot.xbounds shouldBe Bounds(0, 1)
      plot.ybounds shouldBe Bounds(0, 2)
    }

    it("should work an empty row") {
      val plot = Heatmap(Seq(Seq.empty))
      val extent = Extent(100, 200)
      plot.render(extent).extent shouldBe extent
    }

    it("should work with no data") {
      val plot = Heatmap(Seq.empty)
      val extent = Extent(100, 200)
      plot.render(extent).extent shouldBe extent
    }
  }
}
