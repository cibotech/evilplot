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

package com.cibo.evilplot.numeric

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class AxisDescriptorSpec extends AnyFunSpec with Matchers {

  describe("Ticks") {

    it("returns a nice number, with rounding") {
      (1 to 10).map(num => AxisDescriptor.nicenum(num, round = true)) shouldEqual
        Seq(1.0, 2.0, 5.0, 5.0, 5.0, 5.0, 10.0, 10.0, 10.0, 10.0)
    }

    it("returns a nice number, with ceiling") {
      (1 to 10).map(num => AxisDescriptor.nicenum(num, round = false)) shouldEqual
        Seq(1.0, 2.0, 5.0, 5.0, 5.0, 10.0, 10.0, 10.0, 10.0, 10.0)
    }

    it("gives nice ticks, spanning 0") {
      val ticks = ContinuousAxisDescriptor(Bounds(-3500, 6100), 5, fixed = false)
      ticks.tickMin shouldEqual -4000
      ticks.tickMax shouldEqual 8000
      ticks.spacing shouldEqual 2000
      ticks.numFrac shouldEqual 0
    }

    it("doesn't update bounds when fixed") {
      val ex1 = ContinuousAxisDescriptor(Bounds(-0.05, 0.55), 10, fixed = true)
      ex1.tickMin shouldBe -0.05
      ex1.tickMax shouldBe 0.55
      ex1.spacing shouldBe 0.05

      val ex2 = ContinuousAxisDescriptor(Bounds(-3500, 6100), 5, fixed = true)
      ex2.tickMin shouldBe -3500
      ex2.tickMax shouldBe 6100
      ex2.spacing shouldBe 2000
    }

    it("gives nice ticks, with small numbers") {
      val ticks = ContinuousAxisDescriptor(Bounds(-0.01, 0.07), 3, fixed = false)
      ticks.tickMin shouldEqual -0.02
      ticks.tickMax shouldEqual 0.08
      ticks.spacing shouldEqual 0.02
      ticks.numFrac shouldEqual 2
    }

    it("does not fail when the min and max are the same within machine precision") {
      val ticks = ContinuousAxisDescriptor(Bounds(0.5, 0.5), 10, fixed = false)
      ticks.tickMin shouldBe 0.0 +- AxisDescriptor.machineEpsilonIEEE754Double
      ticks.tickMax shouldBe 1.0 +- AxisDescriptor.machineEpsilonIEEE754Double
      ticks.numFrac shouldBe 1
    }

    it("does not fail when the axisBounds evaluate to Bounds(NaN, NaN)") {
      val ticks = ContinuousAxisDescriptor(Bounds(Double.NaN, Double.NaN), 10, fixed = false)
      ticks.tickMin.isNaN shouldBe true
      ticks.tickMax.isNaN shouldBe true
      ticks.numFrac shouldBe 0
    }

    it("works with fixed bounds and an empty range") {
      val ticks = ContinuousAxisDescriptor(Bounds(0.5, 0.5), 10, fixed = true)
      ticks.numFrac shouldBe 0
    }
  }

}
