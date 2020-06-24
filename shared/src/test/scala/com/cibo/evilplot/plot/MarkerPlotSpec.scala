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

import com.cibo.evilplot.demo.DemoPlots.{plotAreaSize, theme}
import com.cibo.evilplot.geometry.{Extent, Rect, Rotate, Style, Text, Wedge}
import com.cibo.evilplot.numeric.{Bounds, Point}
import com.cibo.evilplot.plot.components.{Marker, Position}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class MarkerPlotSpec extends AnyFunSpec with Matchers {

  describe("Marker Plot") {
    it("overlay marker displays correctly") {
      val marker = Marker(Position.Overlay, _ => Rect(25), Extent(25, 25), 0, 0)
      val data = Seq(Point(-1, 10), Point(20, -5))
      val plot =
        ScatterPlot(data, xBoundBuffer = Some(0.1), yBoundBuffer = Some(0.1)).component(marker)

      plot.xbounds.min should be < -1.0
      plot.xbounds.max should be > 20.0
      plot.ybounds.min should be < -5.0
      plot.ybounds.max should be > 10.0
      marker.size.height should be < 26.0
      marker.size.width should be < 26.0
      marker.x shouldBe 0
      marker.y shouldBe 0
    }

  }

}
