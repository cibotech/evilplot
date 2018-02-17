package com.cibo.evilplot.plot.components

import com.cibo.evilplot.numeric.{Bounds, Point}
import org.scalatest.{FunSpec, Matchers}

class FunctionPlotLineSpec extends FunSpec with Matchers {
  describe("Plotting a function: plottablePoints") {
    val bounds = Bounds(0, 1)
    def inBounds(p: Point): Boolean = bounds.isInBounds(p.x) && bounds.isInBounds(p.y)
    val out = Vector(Point(.1, 1.5), Point(.5, -1.1), Point(.8, -2))
    val in = Vector(Point(.1, 0.4), Point(.4, .5), Point(.4, .4))

    it("should return no points when all points are out of bounds.") {
      FunctionPlotLine.plottablePoints(out, inBounds) shouldBe empty
    }

    it("should return all points when none are out of bounds") {
      FunctionPlotLine.plottablePoints(in, inBounds) shouldBe Vector(in)
    }

    it("should return separate paths for the points in bounds when the first few points are out of bounds") {
      val actual = FunctionPlotLine.plottablePoints(out ++ in ++ out ++ in, inBounds)
      val expected = Vector(in, in)
      actual shouldBe expected
    }

    it("should return separate paths for the points in bounds when the first few points are in bounds") {
      val actual = FunctionPlotLine.plottablePoints(in ++ out ++ in ++ out, inBounds)
      val expected = Vector(in, in)
      actual shouldBe expected
    }
  }
}
