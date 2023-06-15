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

import com.cibo.evilplot.numeric.Point
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class ScatterPlotSpec extends AnyFunSpec with Matchers {

  import com.cibo.evilplot.plot.aesthetics.DefaultTheme._

  describe("ScatterPlot") {
    it("sets adheres to bound buffers") {
      val data = Seq(Point(-1, 10), Point(20, -5))
      val plot = ScatterPlot(data, xBoundBuffer = Some(0.1), yBoundBuffer = Some(0.1))

      plot.xbounds.min should be < -1.0
      plot.xbounds.max should be > 20.0
      plot.ybounds.min should be < -5.0
      plot.ybounds.max should be > 10.0
    }

    it("sets exact bounds without buffering") {
      val data = Seq(Point(-1, 10), Point(20, -5))
      val plot = ScatterPlot(data)

      plot.xbounds.min shouldBe -1.0
      plot.xbounds.max shouldBe 20.0
      plot.ybounds.min shouldBe -5.0
      plot.ybounds.max shouldBe 10.0
    }

    it("sets reasonable bounds with only 1 point") {
      val plot = ScatterPlot(Seq(Point(2, 3)))
      plot.xbounds.min shouldBe 2.0 +- 0.0000001
      plot.xbounds.max shouldBe 2.0 +- 0.0000001
      plot.ybounds.min shouldBe 3.0 +- 0.0000001
      plot.ybounds.max shouldBe 3.0 +- 0.0000001
    }
  }
}
