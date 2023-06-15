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

import com.cibo.evilplot.numeric.KernelDensityEstimation._
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import scala.util.Random.nextDouble

class KernelDensityEstimationSpec extends AnyFunSpec with Matchers {
  describe("KernelDensityEstimation") {
    it("should properly calculate probability densities from the normal distribution") {
      val doubles: Seq[Double] = Seq(0.383738345, 0.678363183, 0.870892648, 0.955542032,
        0.739779717, 0.495273777, 0.346604271, 0.971385358, 0.998761496, 0.222603808, 0.370077565,
        0.081424898, 0.775284522, 0.005343148, 0.470091059, 0.510200712, 0.361834899, 0.259037336,
        0.806185498, 0.337947191)

      val densities: Seq[Double] = Seq(0.3706244, 0.3169451, 0.2730322, 0.2527211, 0.3034387,
        0.3528943, 0.3756844, 0.2488927, 0.2422704, 0.3891794, 0.3725376, 0.3976220, 0.2953862,
        0.3989366, 0.3572100, 0.3502560, 0.3736631, 0.3857797, 0.2882561, 0.3767993)

      (doubles zip densities).foreach {
        case (x, d) =>
          probabilityDensityInNormal(x) shouldEqual d +- 1e-5
      }
    }
  }

  describe("outer product calculation") {
    it("should calculate correctly") {
      val xs = Vector(-.3015008, 0.6520850)
      val ys = Vector(-.3033709, 0.6459446, 1.7718656)

      val outer =
        Array(Array(0.09146657, -0.1947528, -0.5342189), Array(-0.19782361, 0.4212108, 1.1554070))
      (outerProduct(xs.toArray, ys.toArray).flatten.toSeq zip outer.flatten.toSeq) foreach {
        case (calculated, actual) => calculated shouldBe actual +- 1e-6
      }
    }
  }

  describe("bandwidth estimation") {
    it("should always be non-negative") { // no ScalaCheck in scala.js ?
      (0 until 10) map (_ => Vector.fill(10)(nextDouble)) foreach (bandwidthEstimate(_) should be >= 0.0)
    }

    it("should be calculated properly for some sample vectors") {
      val xs = Vector(-1.06575970, 0.42420074, 0.02938372, 2.04974410, -1.63546604, 0.27436596,
        -0.90455302, 0.86564478, 1.68234299, 0.19371170)
      val ys = Vector(-0.7695360, 0.5401861, 0.3025197, 1.8889234, 1.1587218, -0.6744424, 0.9437049)

      bandwidthEstimate(xs) shouldBe 2.847659 +- 1e-6
      bandwidthEstimate(ys) shouldBe 2.652604 +- 1e-6
    }

    it("should reutrn NaN on a one element vector") {
      val xs = Vector(-1.045696)
      bandwidthEstimate(xs).isNaN shouldBe true
    }

    it("should return NaN on an empty vector") {
      bandwidthEstimate(Vector[Double]()).isNaN shouldBe true
    }
  }
}
