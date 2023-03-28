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

package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.Extent
import com.cibo.evilplot.numeric.{Bounds, Point}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class TransformUtilsSpec extends AnyFunSpec with Matchers {

  describe("PlotUtils") {

    it("computes the correct buffer") {
      val zeroTen = PlotUtils.boundsWithBuffer(xs = Seq(0.0, 10.0), 0.0)
      zeroTen shouldEqual Bounds(0.0, 10.0)

      val zeroTenTen = PlotUtils.boundsWithBuffer(xs = Seq(0.0, 10.0), 0.1)
      zeroTenTen shouldEqual Bounds(-1.0, 11.0)

      val negZeroTenTen = PlotUtils.boundsWithBuffer(xs = Seq(0.0, -10.0), buffer = 0.1)
      negZeroTenTen shouldEqual Bounds(-11.0, 1.0)
    }

    it("computes bounds") {
      val points = Seq(Point(0.0, 0.0), Point(10.0, 10.0))

      PlotUtils.bounds(points, 0.1) shouldEqual (Bounds(-1.0, 11.0), Bounds(-1.0, 11.0))
      PlotUtils.bounds(points, 0.0, xboundBuffer = Some(0.1)) shouldEqual (Bounds(-1.0, 11.0), Bounds(
        0,
        10.0))
      PlotUtils.bounds(points, 0.0, yboundBuffer = Some(0.1)) shouldEqual (Bounds(0, 10.0), Bounds(
        -1.0,
        11.0))
    }

  }

  describe("TransformWorldToScreen") {

    val xTransformer =
      TransformWorldToScreen.xCartesianTransformer(Bounds(0, 100), extent = Extent(100, 100))
    val yTransformer =
      TransformWorldToScreen.yCartesianTransformer(Bounds(0, 100), extent = Extent(100, 100))

    it("default x transformer works properly") {

      xTransformer(-100) shouldEqual -100.0 +- 0.000001
      xTransformer(0) shouldEqual 0.0 +- 0.000001
      xTransformer(100) shouldEqual 100.0 +- 0.000001

    }

    it("default y transformer works properly") {

      yTransformer(-100) shouldEqual 200.0 +- 0.000001
      yTransformer(0) shouldEqual 100.0 +- 0.000001
      yTransformer(100) shouldEqual 0.0 +- 0.000001
    }

    it("Transforms to screen correctly") {
      import TransformWorldToScreen._
      val transformer =
        TransformWorldToScreen.yCartesianTransformer(Bounds(0, 10), extent = Extent(100, 100))

      transformDatumToWorld(Point(0.0, 0.0), xTransformer, yTransformer) shouldEqual Point(
        0.0,
        100.0)
    }
  }
}
