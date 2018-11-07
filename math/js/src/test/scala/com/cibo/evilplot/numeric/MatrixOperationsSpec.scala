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

import org.scalatest.{FunSpec, Matchers}

class KernelDensityEstimationSpec extends FunSpec with Matchers {
  describe("KernelDensityEstimation") {

    it("should properly calculate the matrix product A * B^T") {
      val a = Array(
        Array(0.9477583, 0.7026756, 0.0075461, 0.8175592),
        Array(0.5654393, 0.7140698, 0.5457264, 0.1904566),
        Array(0.8049051, 0.5844244, 0.5987555, 0.1988892),
        Array(0.6323643, 0.2691138, 0.7707659, 0.4891442),
        Array(0.2572372, 0.6319369, 0.2961405, 0.8173221)
      )

      val b = Array(
        Array(0.95817, 0.72988, 0.39111, 0.51126),
        Array(0.42121, 0.98949, 0.41734, 0.76212),
        Array(0.55427, 0.47121, 0.73324, 0.42507),
        Array(0.98662, 0.85474, 0.22522, 0.52602),
        Array(0.94610, 0.54784, 0.21054, 0.92127)
      )

      val answer = Array(
        Array(1.8419, 1.7207, 1.2095, 1.9674, 2.0364),
        Array(1.3738, 1.3176, 1.1310, 1.3913, 1.2165),
        Array(1.5337, 1.3188, 1.2451, 1.5331, 1.3910),
        Array(1.3539, 1.2271, 1.2504, 1.2848, 1.3586),
        Array(1.2414, 1.4801, 1.0049, 1.2906, 1.4049)
      )
      val calculatedResult = MatrixOperations.matrixMatrixTransposeMult(a, b).flatten.toSeq
      (calculatedResult zip answer.flatten.toSeq).foreach {
        case (calculated, actual) => calculated shouldEqual actual +- 0.003
      }
    }
  }
}
