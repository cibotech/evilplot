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
