/*
 * Copyright (c) 2018, CiBO Technologies, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.cibo.evilplot.plot.components

import com.cibo.evilplot.numeric.{Bounds, Point}
import org.scalactic.{Equality, TolerantNumerics}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class FunctionPlotLineSpec extends AnyFunSpec with Matchers {

  implicit val doubleEquality: Equality[Double] =
    TolerantNumerics.tolerantDoubleEquality(1e-10)

  implicit object VectorDoubleEquivalence extends Equality[Vector[Double]] {
    def areEqual(a: Vector[Double], b: Any): Boolean = b match {
      case bx: Vector[_] => a.corresponds(bx)((i, j) => doubleEquality.areEqual(i, j))
      case _             => false
    }
  }

  describe("plottablePoints") {
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

    it(
      "should return separate paths for the points in bounds when the first few points are out of bounds") {
      val actual = FunctionPlotLine.plottablePoints(out ++ in ++ out ++ in, inBounds)
      val expected = Vector(in, in)
      actual shouldBe expected
    }

    it(
      "should return separate paths for the points in bounds when the first few points are in bounds") {
      val actual = FunctionPlotLine.plottablePoints(in ++ out ++ in ++ out, inBounds)
      val expected = Vector(in, in)
      actual shouldBe expected
    }
  }

  describe("pointsForFunction") {
    it("should return an empty vector when numPoints is 0") {
      val points = FunctionPlotLine.pointsForFunction(x => x, Bounds(-5, 5), 0)

      points shouldBe Vector.empty[Point]
    }

    it("should use the minimum bound when numPoints is 1") {
      val points = FunctionPlotLine.pointsForFunction(x => x, Bounds(-5, 5), 1)

      points.length shouldBe 1
      points.head shouldBe Point(-5, -5)
    }

    it("should use the min and max bound when numPoints is 2") {
      val points = FunctionPlotLine.pointsForFunction(x => x, Bounds(-5, 5), 2)

      points.length shouldBe 2
      points(0) shouldBe Point(-5, -5)
      points(1) shouldBe Point(5, 5)
    }

    it("should divide the range evenly") {
      val points = FunctionPlotLine.pointsForFunction(x => x, Bounds(-5, 5), 3)

      points.length shouldBe 3
      points(0) shouldBe Point(-5, -5)
      points(1) shouldBe Point(0, 0)
      points(2) shouldBe Point(5, 5)
    }

    it("should deal with Double precision issues") {
      val points = FunctionPlotLine.pointsForFunction(x => x, Bounds(0, 1), 11)

      points.length shouldBe 11
      points.map(_.x) shouldEqual Vector(0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0)
    }

    it("should still return the max bound point for large sequences of small x steps") {
      val points = FunctionPlotLine.pointsForFunction(x => x, Bounds(0, 0.001), 1000)

      points.length shouldBe 1000
      points.head shouldBe Point(0, 0)
      points.last shouldBe Point(0.001, 0.001)
    }

    it("should handle cases where a Range would stop short") {
      val points = FunctionPlotLine.pointsForFunction(x => x, Bounds(2.2, 3.6), 10)

      points.length shouldBe 10
      points.head shouldBe Point(2.2, 2.2)
      points.last shouldBe Point(3.6, 3.6)
    }

    it("should return the correct points") {
      val pts = FunctionPlotLine.pointsForFunction(x => x * x, Bounds(0, 1), 5)
      pts.head.x shouldBe 0.0 +- math.ulp(1.0)
      pts.head.y shouldBe 0.0 +- math.ulp(1.0)
      pts.last.x shouldBe 1.0 +- math.ulp(1.0)
      pts.last.y shouldBe 1.0 +- math.ulp(1.0)
    }
  }
}
