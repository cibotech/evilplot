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

import com.cibo.evilplot.numeric.Labeling.LabelingResult
import org.scalacheck.Gen
import org.scalacheck.Prop.{forAll, forAllNoShrink}
import org.scalatest.prop.Checkers
import org.scalatest.{FunSpec, Inside, Matchers, OptionValues}

class LabelingSpec extends FunSpec with Matchers with Checkers with OptionValues with Inside {
  private val boundsGen = for {
    x <- Gen.chooseNum[Double](-10000, 10000)
    y <- Gen.posNum[Double]
  } yield Bounds(x, x + y)

  private val nticksGen = Gen.chooseNum[Int](2, 12)

  describe("Axis labeling") {
    it("should not produce misleadingly formatted labels") {
      val labeling = Labeling.label(Bounds(-.72, .47), fixed = true)
      val labelsAsDoubles = labeling.labels.map(_.toDouble)
      labelsAsDoubles.zip(labeling.values).foreach {
        case (text, value) =>
          text shouldBe value +- 0.0001
      }
    }

    it("should pad the data range when min == max and bounds are not fixed") {
      val labeling = Labeling.label(Bounds(2.2, 2.2))
      labeling.bounds.min shouldBe 1.7 +- math.ulp(1.0)
      labeling.bounds.max shouldBe 2.7 +- math.ulp(1.0)
    }

    ignore("should work with fixed bounds and 0 range") {
      val bounds = Bounds(2.2, 2.2)
      val labeling = Labeling.label(bounds, fixed = true)
      labeling.bounds shouldBe bounds
      labeling.axisBounds shouldBe bounds
    }

    it("should work with 0 ticks") {
      val labeling = Labeling.label(Bounds(-.323, .525), numTicks = Some(0))
      labeling.values shouldBe empty
      labeling.labels shouldBe empty
    }

    it("should produce no ticks when bounds are NaN and 0 ticks are requested") {
      val labeling = Labeling.label(Bounds(Double.NaN, Double.NaN), numTicks = Some(0))
      labeling.values shouldBe empty
      labeling.labels shouldBe empty
    }

    it("should use the midpoint of the bounds when only one tick is requested") {
      val labeling = Labeling.label(Bounds(0, 1), numTicks = Some(1))
      labeling.values should have length 1
      labeling.values.head shouldBe 0.5
      labeling.labels should have length 1
      labeling.labels.head shouldBe "0.5"
    }

    it("should use max and min for two ticks") {
      val labeling = Labeling.label(Bounds(0, 1), numTicks = Some(2))
      labeling.values should have length 2
      labeling.values should contain theSameElementsInOrderAs Seq(0d, 1d)
      labeling.labels should have length 2
      labeling.labels should contain theSameElementsInOrderAs Seq(0d.toString, 1d.toString)
    }

    it("should use a naive labeling when the optimization scheme cannot find a good one") {
      val labeling = Labeling.label(Bounds(0, 1.0), numTicks = Some(12), fixed = true)
      labeling.axisBounds.min shouldBe 0.0
      labeling.axisBounds.max shouldBe 1.0
      labeling.numTicks shouldBe 12
      labeling.values(1) shouldBe 0.090909 +- 1e6

    }

    it("should not update the axis bounds when fixing") {
      val labeling = Labeling.label(Bounds(-0.05, 0.55), fixed = true)
      labeling.axisBounds shouldBe Bounds(-0.05, 0.55)
    }

    it("should produce a labeling when passed NaN bounds") {
      val labeling = Labeling.label(Bounds(Double.NaN, Double.NaN))
      Double.box(labeling.axisBounds.min) shouldBe 'isNaN
      Double.box(labeling.axisBounds.max) shouldBe 'isNaN
      noException shouldBe thrownBy(labeling.labels)
    }

    it("handles negative tick num requests with an IllegalArgumentException") {
      an[IllegalArgumentException] should be thrownBy Labeling
        .label(Bounds(0, 1), numTicks = Some(-1))
    }

    it("handles invalid bounds (min > max) with an IllegalArgumentException") {
      an[IllegalArgumentException] should be thrownBy Labeling.label(Bounds(1, 0))
    }

    it("throws an IllegalArgumentException when given an empty nice number list") {
      an[IllegalArgumentException] should be thrownBy Labeling.label(Bounds(0, 1), nicenums = Seq())
    }

    it("should produce exactly the number of ticks specified when requested (unfixed bounds)") {
      check(forAll(boundsGen, nticksGen) {
        case (bounds, nticks) =>
          val labeling = Labeling.label(bounds, numTicks = Some(nticks))
          labeling.numTicks == nticks
      })
    }

    it("should produce a non-naive labeling with the default parameters") {
      check(forAll(boundsGen) { bounds =>
        Labeling.label(bounds).isInstanceOf[LabelingResult]
      })
    }

    it("should produce fixed bounds labelings for which adding additional ticks is not possible") {
      check(forAll(boundsGen, Gen.option(nticksGen)) {
        case (bounds, nticks) =>
          inside(Labeling.label(bounds, nticks, fixed = true)) {
            case LabelingResult(_, axis, label, _, spacing, _) =>
              label.max + spacing >= axis.max
            case naive: AxisDescriptor =>
              (naive.axisBounds.max === naive.bounds.max +- math.ulp(1.0)) &&
                naive.axisBounds.min === naive.bounds.min +- math.ulp(1.0)
          }
      })
    }

    it("should produce exactly the number of ticks specified when requested (fixed bounds)") {
      check(forAllNoShrink(boundsGen, nticksGen) {
        case (bounds, nticks) =>
          val labeling = Labeling.label(bounds, numTicks = Some(nticks), fixed = true)
          labeling.numTicks == nticks
      })
    }
  }
}
