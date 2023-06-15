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

import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent, Rect}
import com.cibo.evilplot.numeric.Point
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.components.{FacetedPlotComponent, Position}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class FacetsSpec extends AnyFunSpec with Matchers {

  import com.cibo.evilplot.plot.aesthetics.DefaultTheme._

  describe("Facets") {
    it("is the correct size with one facet") {
      val inner = ScatterPlot(Seq(Point(1, 1), Point(2, 2)))
      val faceted = Facets(Seq(Seq(inner)))

      faceted.xbounds shouldBe inner.xbounds
      faceted.ybounds shouldBe inner.ybounds

      val extent = Extent(600, 400)
      faceted.plotExtent(extent) shouldBe inner.plotExtent(extent)
    }

    it("works with rows of differing sizes") {
      val inner1 = ScatterPlot(Seq(Point(1, 1), Point(2, 2)))
      val inner2 = ScatterPlot(Seq(Point(2, 1), Point(4, 2)))
      val inner3 = ScatterPlot(Seq(Point(3, 1), Point(5, 2)))
      val faceted = Facets(Seq(Seq(inner1, inner2), Seq(inner3)))

      val extent = Extent(600, 400)
      faceted.render(extent).extent shouldBe extent
    }

    it("is the correct size with a title") {
      val titleHeight = 10
      val inner = ScatterPlot(Seq(Point(1, 1), Point(2, 2)))
      val faceted = Facets(
        Seq(
          Seq(inner, inner),
          Seq(inner, inner),
          Seq(inner, inner)
        )
      ).title(Rect(1, titleHeight))

      faceted.xbounds shouldBe inner.xbounds
      faceted.ybounds shouldBe inner.ybounds

      val extent = Extent(600, 400)
      faceted.plotExtent(extent) shouldBe Extent(extent.width, extent.height - titleHeight)
    }

    it("has the right plotOffset.x") {

      val inner1 = ScatterPlot(Seq(Point(1, 1), Point(2, 2)))
      val inner2 = ScatterPlot(Seq(Point(1, 1), Point(2, 2)))

      // Plot component that is larger for `inner2` than `inner1`.
      object TestComponent extends FacetedPlotComponent {
        val position: Position = Position.Left
        override val repeated: Boolean = true
        override def size(plot: Plot): Extent = if (plot == inner2) Extent(10, 10) else Extent(0, 0)
        def render(plot: Plot, extent: Extent, row: Int, column: Int)(
          implicit theme: Theme): Drawable =
          EmptyDrawable()
      }

      val faceted = Facets(Seq(Seq(inner1), Seq(inner2))) :+ TestComponent
      faceted.plotOffset.x shouldBe 10
    }

    it("throws an exception with no facets") {
      an[IllegalArgumentException] should be thrownBy Facets(Seq.empty)
    }
  }
}
