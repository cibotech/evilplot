package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.Extent
import com.cibo.evilplot.numeric.{Bounds, Point}
import org.scalatest.{FunSpec, Matchers}

class XyPlotSpec extends FunSpec with Matchers {

  import com.cibo.evilplot.plot.aesthetics.DefaultTheme._

  describe("XyPlot") {
    it("has the right bounds") {
      val plot = XyPlot(Seq(Point(1, 2), Point(3, 4)), xboundBuffer = Some(0), yboundBuffer = Some(0))
      plot.xbounds shouldBe Bounds(1, 3)
      plot.ybounds shouldBe Bounds(2, 4)
    }

    it("works with no data") {
      val plot = XyPlot(Seq.empty)
      val extent = Extent(100, 200)
      plot.render(extent).extent shouldBe Extent(100, 200)
    }
  }
}
