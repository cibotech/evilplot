package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent}
import com.cibo.evilplot.numeric.{Bounds, Point}
import com.cibo.evilplot.plot.renderers.PlotRenderer
import org.scalatest.{FunSpec, Matchers}

class OverlaySpec extends FunSpec with Matchers {
  describe("Overlay") {
    it("it gets the bounds right for a single plot") {
      val inner = ScatterPlot(Seq(Point(1.0, 10.0), Point(2.0, 20.0)))
      val overlay = Overlay(inner)
      overlay.xbounds shouldBe inner.xbounds
      overlay.ybounds shouldBe inner.ybounds
    }

    it("combines bounds from multiple plots") {
      val inner1 = ScatterPlot(Seq(Point(10.0, -1.0)))
      val inner2 = ScatterPlot(Seq(Point(3.0, 20.0)))
      val overlay = Overlay(inner1, inner2)
      overlay.xbounds shouldBe Bounds(inner2.xbounds.min, inner1.xbounds.max)
      overlay.ybounds shouldBe Bounds(inner1.ybounds.min, inner2.ybounds.max)
    }

    it("occupies the right extents") {
      val inner1 = ScatterPlot(Seq(Point(10.0, -1.0)))
      val inner2 = ScatterPlot(Seq(Point(11.0, 1.0)))
      val overlay = Overlay(inner1, inner2)
      val extent = Extent(600, 400)
      overlay.render(extent).extent shouldBe extent
    }

    it("updates bounds on subplots") {
      var xbounds: Bounds = Bounds(0, 0)
      var ybounds: Bounds = Bounds(0, 0)

      val testRenderer = new PlotRenderer[Int] {
        def render(plot: Plot[Int], plotExtent: Extent): Drawable = {
          xbounds = plot.xbounds
          ybounds = plot.ybounds
          EmptyDrawable(plotExtent)
        }
      }

      val inner = new Plot[Int](
        data = 1,
        xbounds = Bounds(1, 2),
        ybounds = Bounds(3, 4),
        renderer = testRenderer
      )

      val overlay = Overlay(inner)
      val updated = overlay.xbounds(5, 6).ybounds(7, 8)
      updated.render(Extent(100, 200))

      xbounds shouldBe Bounds(5, 6)
      ybounds shouldBe Bounds(7, 8)
    }
  }
}
