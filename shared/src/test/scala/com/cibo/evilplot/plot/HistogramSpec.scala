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
import com.cibo.evilplot.numeric.Bounds
import org.scalatest.{FunSpec, Matchers}

class HistogramSpec extends FunSpec with Matchers {

  import com.cibo.evilplot.plot.aesthetics.DefaultTheme._

  describe("Histogram") {
    val plot = Histogram(Seq(1.0, 1, 1, 2, 3, 4, 4, 5), boundBuffer = Some(0))

    it("has the right bounds") {
      plot.xbounds shouldBe Bounds(1, 5)
      plot.ybounds shouldBe Bounds(0, 3)
    }

    it("has the right extents") {
      val extent = Extent(300, 400)
      plot.render(extent).extent.width shouldBe extent.width +- 1e-6
      plot.render(extent).extent.height shouldBe extent.height +- 1e-6
    }

    it("works with no data") {
      val extent = Extent(100, 200)
      val emptyPlot = Histogram(Seq.empty)
      emptyPlot.render(extent).extent shouldBe extent
    }

  }
}
