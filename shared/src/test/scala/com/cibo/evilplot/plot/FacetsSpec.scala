package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent, Rect}
import com.cibo.evilplot.numeric.Point
import org.scalatest.{FunSpec, Matchers}
import com.cibo.evilplot.plot.components.{FacetedPlotComponent, Position}

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

    it("works with rows of differing sizes") {
      val inner1 = ScatterPlot(Seq(Point(1, 1), Point(2, 2)))
      val inner2 = ScatterPlot(Seq(Point(2, 1), Point(4, 2)))
      val inner3 = ScatterPlot(Seq(Point(3, 1), Point(5, 2)))
      val faceted = Facets(Seq(Seq(inner1, inner2), Seq(inner3)))

      val extent = Extent(600, 400)
      faceted.render(extent).extent shouldBe extent
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

    it("has the right plotOffset.x") {

      val inner1 = ScatterPlot(Seq(Point(1, 1), Point(2, 2)))
      val inner2 = ScatterPlot(Seq(Point(1, 1), Point(2, 2)))

      // Plot component that is larger for `inner2` than `inner1`.
      object TestComponent extends FacetedPlotComponent {
        val position: Position = Position.Left
        override val repeated: Boolean = true
        override def size(plot: Plot): Extent = if (plot == inner2) Extent(10, 10) else Extent(0, 0)
        def render(plot: Plot, extent: Extent, row: Int, column: Int): Drawable = EmptyDrawable()
      }

      val faceted = Facets(Seq(Seq(inner1), Seq(inner2))) :+ TestComponent
      faceted.plotOffset.x shouldBe 10
    }
  }
}
