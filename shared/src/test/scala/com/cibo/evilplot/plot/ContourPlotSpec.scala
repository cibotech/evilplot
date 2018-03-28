package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.Extent
import com.cibo.evilplot.numeric.{Bounds, Point}
import org.scalatest.{FunSpec, Matchers}

class ContourPlotSpec extends FunSpec with Matchers {

  import com.cibo.evilplot.plot.aesthetics.DefaultTheme._

  describe("ContourPlot") {
    it("it has the right bounds") {
      val plot = ContourPlot(Seq(Point(1, 2), Point(3, 4)), boundBuffer = Some(0.0))
      plot.xbounds shouldBe Bounds(1, 3)
      plot.ybounds shouldBe Bounds(2, 4)
    }

    it("works with no data") {
      val plot = ContourPlot(Seq.empty)
      val extent = Extent(100, 200)
      plot.render(extent).extent shouldBe extent
    }
  }
}
