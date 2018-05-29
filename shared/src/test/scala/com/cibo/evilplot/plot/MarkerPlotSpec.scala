package com.cibo.evilplot.plot

import com.cibo.evilplot.demo.DemoPlots.{plotAreaSize, theme}
import com.cibo.evilplot.geometry.{Extent, Rect, Rotate, Style, Text, Wedge}
import com.cibo.evilplot.numeric.{Bounds, Point}
import com.cibo.evilplot.plot.components.{Marker, Position}
import org.scalatest.{FunSpec, Matchers}

class MarkerPlotSpec extends FunSpec with Matchers {

  describe("Marker Plot") {
    it("overlay marker displays correctly") {
      val marker = Marker(Position.Overlay, _ => Rect(25), Extent(25, 25), 0, 0)
      val data = Seq(Point(-1, 10), Point(20, -5))
      val plot = ScatterPlot(data, boundBuffer = Some(0.1)).component(marker)

      plot.xbounds.min should be < -1.0
      plot.xbounds.max should be > 20.0
      plot.ybounds.min should be < -5.0
      plot.ybounds.max should be > 10.0
      marker.size.height should be < 26.0
      marker.size.width should be < 26.0
      marker.x shouldBe 0
      marker.y shouldBe 0
    }

  }

}
