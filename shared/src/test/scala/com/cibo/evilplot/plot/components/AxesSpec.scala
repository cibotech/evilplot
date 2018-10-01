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
import com.cibo.evilplot.plot.{Bar, BarChart, ScatterPlot}
import org.scalatest.{FunSpec, Matchers}

class AxesSpec extends FunSpec with Matchers {

  describe("discrete X") {
    it("should set the default bounds") {
      val plot = BarChart(Seq(3.0, 4)).xAxis()
      plot.xbounds shouldBe Bounds(0, 2)
    }

    it("should set bounds for labels") {
      val plot = BarChart(Seq(3.0, 4)).xAxis(Seq("one", "two"))
      plot.xbounds shouldBe Bounds(0, 2)
    }

    it("should set bounds for more labels") {
      val plot = BarChart(Seq(3.0, 4)).xAxis(Seq("one", "two", "three"))
      plot.xbounds shouldBe Bounds(0, 3)
    }

    it("should set bounds for fewer labels") {
      val plot = BarChart(Seq(3.0, 4)).xAxis(Seq("one"))
      plot.xbounds shouldBe Bounds(0, 1)
    }
  }

  describe("continuous X") {
    it("should set reasonable default bounds") {
      val plot = ScatterPlot(Seq(Point(3, 4), Point(5, 6)), boundBuffer = Some(0)).xAxis()
      plot.xbounds shouldBe Bounds(3, 5)
    }

    it("should not update the bounds multiple times") {
      val plot = ScatterPlot(Seq(Point(0, 0), Point(1.007, 2)), boundBuffer = Some(0))
        .xbounds(0, 1.1)
        .xAxis()
      plot.xbounds.min shouldBe 0.0 +- 1e-6
      plot.xbounds.max shouldBe 1.1 +- 1e-6
    }
  }

  describe("continuous Y") {
    it("should set reasonable default bounds") {
      val plot = ScatterPlot(Seq(Point(3, 4), Point(5, 6)), boundBuffer = Some(0)).yAxis()
      plot.ybounds shouldBe Bounds(4, 6)
    }
  }
}
