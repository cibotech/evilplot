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
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class HeatmapSpec extends AnyFunSpec with Matchers {

  import com.cibo.evilplot.plot.aesthetics.DefaultTheme._

  describe("Heatmap") {
    it("has the right bounds") {
      val plot = Heatmap(Seq(Seq(1), Seq(2)))
      plot.xbounds shouldBe Bounds(0, 1)
      plot.ybounds shouldBe Bounds(0, 2)
    }

    it("should work an empty row") {
      val plot = Heatmap(Seq(Seq.empty))
      val extent = Extent(100, 200)
      plot.render(extent).extent shouldBe extent
    }

    it("should work with no data") {
      val plot = Heatmap(Seq.empty)
      val extent = Extent(100, 200)
      plot.render(extent).extent shouldBe extent
    }
  }
}
