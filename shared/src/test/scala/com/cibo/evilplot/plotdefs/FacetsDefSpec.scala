/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.plotdefs

import com.cibo.evilplot.numeric.{Bounds, BoxPlotSummaryStatistics, Point}
import org.scalatest.{FunSpec, Matchers}

class FacetsDefSpec extends FunSpec with Matchers {
  describe("FacetsDefSpec") {
    /* Let's make this scene:
     * y = x   | y = x^2
     * y = sin(x) | y = cos(x)
     * y = x^3 | y = -x
     */
    val fs: Seq[(Double => Double)] = Seq(x => x, x => x * x, x => math.sin(x), x => math.cos(x), x => math.pow(x, 3),
      x => -x)
    val range = -1.0 to 1.0 by 0.05

    val pds = fs.map(f => (range zip range.map(f)).map{ case (x, y) => Point(x, y) }).map(d => ScatterPlotDef(d))

    it("should place X axes only on the plots in the bottom row (FixedX)") {
      val facetsDef = FacetsDef(3, 2, pds, None, None, FixedX(), None, PlotOptions())
      val bottomPlots = facetsDef.plotDefs.drop(4)
      val otherPlots = facetsDef.plotDefs.take(4)
      for (plotDef <- bottomPlots) {
        plotDef.options.drawXAxis shouldBe true
      }
      for (plotDef <- otherPlots) {
        plotDef.options.drawXAxis shouldBe false
      }
    }

    it("should place Y axes only on the plots in the left column (FixedY)") {
      val facetsDef = FacetsDef(3, 2, pds, None, None, FixedY(), None, PlotOptions())
      val leftPlots = facetsDef.plotDefs.grouped(2).map(_.head)
      val otherPlots = facetsDef.plotDefs.grouped(2).map(_.tail.head)
      for (plotDef <- leftPlots) {
        plotDef.options.drawYAxis shouldBe true
      }

      for (plotDef <- otherPlots) {
        plotDef.options.drawYAxis shouldBe false
      }
    }

    it("should, when requested, fix to the correct (widest) bounds") {
      import FacetsDefFunctions._
      val ps: Seq[Double] = Seq(-5, 1500, 32)
      val qs: Seq[Double] = Seq(-1000, 4, 2)

      val boxPlotDef1 = BoxPlotDef(Seq.fill(1)(""), Seq(BoxPlotSummaryStatistics(ps)))
      val boxPlotDef2 = BoxPlotDef(Seq.fill(1)(""), Seq(BoxPlotSummaryStatistics(qs)))
      widestBounds(Seq(boxPlotDef1, boxPlotDef2), yAxis).get shouldBe Bounds(-1000, 1500)
    }

  }
}
