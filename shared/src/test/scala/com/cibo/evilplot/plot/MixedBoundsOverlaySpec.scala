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

import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent}
import com.cibo.evilplot.numeric.{Bounds, Point}
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.renderers.PlotRenderer
import org.scalatest.{FunSpec, Matchers}

class MixedBoundsOverlaySpec extends FunSpec with Matchers {

  implicit val theme = com.cibo.evilplot.plot.aesthetics.Theme.default

  describe("MixedBoundsOverlay") {

    it("it has the bounds that are set for it") {
      val xbounds = Bounds(-2, 2)
      val ybounds = Bounds(100, 200)
      val inner1 = ScatterPlot(Seq(Point(10.0, -1.0)))
      val inner2 = ScatterPlot(Seq(Point(3.0, 20.0)))
      val overlay = MixedBoundsOverlay(xbounds, ybounds, inner1, inner2)
      overlay.xbounds shouldBe xbounds
      overlay.ybounds shouldBe ybounds
    }

    it("occupies the right extents") {
      val inner1 = ScatterPlot(Seq(Point(10.0, -1.0)))
      val inner2 = ScatterPlot(Seq(Point(11.0, 1.0)))
      val overlay = MixedBoundsOverlay(Bounds(0, 1), Bounds(0, 1), inner1, inner2)
      val extent = Extent(600, 400)
      overlay.render(extent).extent shouldBe extent
    }

    it("doesn't update bounds on subplots") {
      var xbounds: Bounds = Bounds(0, 0)
      var ybounds: Bounds = Bounds(0, 0)

      val testRenderer = new PlotRenderer {
        def render(plot: Plot, plotExtent: Extent)(implicit theme: Theme): Drawable = {
          xbounds = plot.xbounds
          ybounds = plot.ybounds
          EmptyDrawable().resize(plotExtent)
        }
      }

      val inner = new Plot(
        xbounds = Bounds(1, 2),
        ybounds = Bounds(3, 4),
        renderer = testRenderer
      )

      val overlay = MixedBoundsOverlay(Bounds(50, 80), Bounds(50, 80), inner)
      val updated = overlay.xbounds(5, 6).ybounds(7, 8)
      updated.render(Extent(100, 200))

      xbounds shouldBe Bounds(1, 2)
      ybounds shouldBe Bounds(3, 4)
    }

    it("should throw an exception with no plots") {
      an[IllegalArgumentException] should be thrownBy MixedBoundsOverlay(Bounds(1, 2), Bounds(1, 2))
    }

  }

}
