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

package com.cibo.evilplot.colors

import com.cibo.evilplot.geometry.GradientFill
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class FillGradientsSpec extends AnyFunSpec with Matchers {

  describe("Gradient distribution functions") {

    it("Should distribute 1 properly") {

      val gradientSeq = FillGradients.distributeEvenly(
        Seq.fill(1)(RGB.random)
      )

      gradientSeq(0).offset shouldEqual 0.0
      gradientSeq(1).offset shouldEqual 1.0
      gradientSeq(0).color shouldEqual gradientSeq.head.color
      gradientSeq(1).color shouldEqual gradientSeq.head.color

    }

    it("Should distribute 2 properly") {

      val gradientSeq = FillGradients.distributeEvenly(
        Seq(HTMLNamedColors.red, HTMLNamedColors.white)
      )

      gradientSeq(0).offset shouldEqual 0.0
      gradientSeq(0).color shouldEqual HTMLNamedColors.red

      gradientSeq(1).offset shouldEqual 1.0
      gradientSeq(1).color shouldEqual HTMLNamedColors.white

    }

    it("Should distribute 3 properly") {

      val gradientSeq = FillGradients.distributeEvenly(
        Seq(HTMLNamedColors.red, HTMLNamedColors.white, HTMLNamedColors.blue)
      )

      gradientSeq(0).offset shouldEqual 0.0
      gradientSeq(0).color shouldEqual HTMLNamedColors.red

      gradientSeq(1).offset shouldEqual 0.5
      gradientSeq(1).color shouldEqual HTMLNamedColors.white

      gradientSeq(2).offset shouldEqual 1.0
      gradientSeq(2).color shouldEqual HTMLNamedColors.blue

    }

    it("Should distribute 4 properly") {

      val gradientSeq = FillGradients.distributeEvenly(
        Seq(
          HTMLNamedColors.red,
          HTMLNamedColors.white,
          HTMLNamedColors.blue,
          HTMLNamedColors.mintCream)
      )

      gradientSeq(0).offset shouldEqual 0.0
      gradientSeq(0).color shouldEqual HTMLNamedColors.red

      gradientSeq(1).offset shouldEqual 0.333 +- 0.01
      gradientSeq(1).color shouldEqual HTMLNamedColors.white

      gradientSeq(2).offset shouldEqual 0.666 +- 0.01
      gradientSeq(2).color shouldEqual HTMLNamedColors.blue

      gradientSeq(3).offset shouldEqual 1.0
      gradientSeq(3).color shouldEqual HTMLNamedColors.mintCream

    }

  }
}
