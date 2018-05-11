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

import org.scalacheck.Gen
import org.scalatest.prop.PropertyChecks
import org.scalatest.{FunSpec, Matchers}

class LabelingSpec extends FunSpec with Matchers with PropertyChecks {
  private val boundsGen = for {
    x <- Gen.chooseNum[Double](-10000, 10000)
    y <- Gen.posNum[Double]
  } yield Bounds(x, x + y)

  private val nticksGen = Gen.chooseNum[Int](2, 12).map(i => Some(i))

  private val boundsNticks = boundsGen.flatMap(b => nticksGen.map(n => (b, n)))

  describe("Axis labeling") {
    it("should produce a flex labeling when requested") {
      val labeling = Labeling.label(Bounds(-.72, .47), fixed = true)
    }

    it("should produce a loose labeling when requested") {
      val labeling = Labeling.label(Bounds(-.74, .42))
    }

    it("should not produce misleadingly formatted labels") {
      val labeling = Labeling.label(Bounds(-.72, .47), fixed = true)
      val labelsAsDoubles = labeling.labels.map(_.toDouble)
      labelsAsDoubles.zip(labeling.values).foreach {
        case (text, value) =>
          text shouldBe value +- 0.0001
      }
    }

    it("should pad the data range when min == max and bounds are not fixed") {}

    it("should work with fixed bounds and 0 range") {}

    ignore("should not update the axis bounds when fixing") {
      val labeling = Labeling.label(Bounds(-0.05, 0.55), fixed = true)
      labeling.axisBounds shouldBe Bounds(-0.05, 0.55)
    }

    it("should produce a labeling when passed NaN bounds") {
      val labeling = Labeling.label(Bounds(Double.NaN, Double.NaN))
      Double.box(labeling.axisBounds.min) shouldBe 'isNaN
      Double.box(labeling.axisBounds.max) shouldBe 'isNaN
      noException shouldBe thrownBy(labeling.labels)
    }

    it("should produce exactly the number of ticks specified when requested (unfixed bounds)") {
      forAll((boundsGen, "bounds")) { bounds =>
        val labeling = Labeling.label(bounds, numTicks = Some(4))
        labeling.numTicks shouldBe 4
      }
    }

    it("should produce exactly the number of ticks specified when requested (fixed bounds)") {
      forAll((boundsNticks, "bounds")) {
        case (bounds, nticks) =>
          val labeling = Labeling.label(bounds, numTicks = nticks, fixed = true)
          Seq(labeling.numTicks) should contain oneOf (2, 3, 4)
      }
    }
  }
}
