package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.{Extent, Rect}
import com.cibo.evilplot.numeric.Point
import org.scalatest.{FunSpec, Matchers}

class FacetsSpec extends FunSpec with Matchers {
  describe("Facets") {
    it("is the correct size with one facet") {
      val inner = ScatterPlot(Seq(Point(1, 1), Point(2, 2)))
      val faceted = Facets(Seq(Seq(inner)))

      faceted.xbounds shouldBe inner.xbounds
      faceted.ybounds shouldBe inner.ybounds

      val extent = Extent(600, 400)
      faceted.plotExtent(extent) shouldBe inner.plotExtent(extent)
    }

    it("is the correct size with a title") {
      val titleHeight = 10
      val inner = ScatterPlot(Seq(Point(1, 1), Point(2, 2)))
      val faceted = Facets(
        Seq(
          Seq(inner, inner),
          Seq(inner, inner),
          Seq(inner, inner)
        )
      ).title(Rect(1, titleHeight))

      faceted.xbounds shouldBe inner.xbounds
      faceted.ybounds shouldBe inner.ybounds

      val extent = Extent(600, 400)
      faceted.plotExtent(extent) shouldBe Extent(extent.width, extent.height - titleHeight)
    }
  }
}
