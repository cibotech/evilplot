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
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class OverlaySpec extends AnyFunSpec with Matchers {

  import com.cibo.evilplot.plot.aesthetics.DefaultTheme._

  describe("Overlay") {
    it("it gets the bounds right for a single plot") {
      val inner = ScatterPlot(Seq(Point(1.0, 10.0), Point(2.0, 20.0)))
      val overlay = Overlay(inner)
      overlay.xbounds shouldBe inner.xbounds
      overlay.ybounds shouldBe inner.ybounds
    }

    it("combines bounds from multiple plots") {
      val inner1 = ScatterPlot(Seq(Point(10.0, -1.0)))
      val inner2 = ScatterPlot(Seq(Point(3.0, 20.0)))
      val overlay = Overlay(inner1, inner2)
      overlay.xbounds shouldBe Bounds(inner2.xbounds.min, inner1.xbounds.max)
      overlay.ybounds shouldBe Bounds(inner1.ybounds.min, inner2.ybounds.max)
    }

    it("occupies the right extents") {
      val inner1 = ScatterPlot(Seq(Point(10.0, -1.0)))
      val inner2 = ScatterPlot(Seq(Point(11.0, 1.0)))
      val overlay = Overlay(inner1, inner2)
      val extent = Extent(600, 400)
      overlay.render(extent).extent shouldBe extent
    }

    it("updates bounds on subplots") {
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

      val overlay = Overlay(inner)
      val updated = overlay.xbounds(5, 6).ybounds(7, 8)
      updated.render(Extent(100, 200))

      xbounds shouldBe Bounds(5, 6)
      ybounds shouldBe Bounds(7, 8)
    }

    it("should throw an exception with no plots") {
      an[IllegalArgumentException] should be thrownBy Overlay()
    }
  }
}
