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
import com.cibo.evilplot.DOMInitializer
import com.cibo.evilplot.numeric.Bounds
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.renderers.PlotRenderer
import org.scalatest.{FunSpec, Matchers}

class PlotSpec extends FunSpec with Matchers {

  import com.cibo.evilplot.plot.aesthetics.DefaultTheme._

  DOMInitializer.init()

  // Renderer to do nothing.
  private[evilplot] case object EmptyPlotRenderer extends PlotRenderer {
    def render(plot: Plot, plotExtent: Extent)(implicit theme: Theme): Drawable =
      EmptyDrawable().resize(plotExtent)
  }

  // Renderer to get the plot extent.
  private case class PlotExtentPlotRenderer() extends PlotRenderer {
    var plotExtentOpt: Option[Extent] = None

    def render(plot: Plot, plotExtent: Extent)(implicit theme: Theme): Drawable = {
      plotExtentOpt = Some(plotExtent)
      EmptyDrawable().resize(plotExtent)
    }
  }

  private def newPlot(
    xbounds: Bounds = Bounds(0, 1),
    ybounds: Bounds = Bounds(0, 1),
    renderer: PlotRenderer = EmptyPlotRenderer
  ): Plot = Plot(xbounds, ybounds, renderer)

  it("should have the right extent") {
    val plot = newPlot()
    val extent = Extent(300, 400)
    plot.render(extent).extent shouldBe extent
  }

  it("should render the full plot area") {
    val extent = Extent(10, 20)
    val renderer = PlotExtentPlotRenderer()
    val plot = newPlot(renderer = renderer)
    plot.render(extent).extent shouldBe extent
    renderer.plotExtentOpt shouldBe Some(extent)
  }

  it("text should reduce the size of the plot area") {
    val extent = Extent(100, 200)
    val renderer = PlotExtentPlotRenderer()
    val plot = newPlot(renderer = renderer).title("Test")
    plot.render(extent).extent shouldBe extent
    renderer.plotExtentOpt.get.height should be < extent.height
    renderer.plotExtentOpt.get.width shouldBe extent.width
  }

  it("a background should not affect the size of the plot area") {
    val extent = Extent(300, 200)
    val renderer = PlotExtentPlotRenderer()
    val plot = newPlot(renderer = renderer).background()
    plot.render(extent).extent shouldBe extent
    renderer.plotExtentOpt.get shouldBe extent
  }

  it("xbounds/ybounds without parens should access bounds") {
    val plot = newPlot(xbounds = Bounds(0, 2), ybounds = Bounds(0, 5))

    plot.xbounds shouldBe Bounds(0, 2)
    plot.ybounds shouldBe Bounds(0, 5)
  }

  it("partial xbounds/ybounds update should work") {
    val plot = newPlot(xbounds = Bounds(0, 2), ybounds = Bounds(0, 5))
    val updatedX = plot.xbounds(lower = -5)
    updatedX.xbounds shouldBe Bounds(-5, 2)
    val doubleUpdatedX = updatedX.xbounds(upper = 5)
    doubleUpdatedX.xbounds shouldBe Bounds(-5, 5)

    val updatedY = plot.ybounds(lower = -7)
    updatedY.ybounds shouldBe Bounds(-7, 5)
    val doubleUpdatedY = updatedY.ybounds(upper = 7)
    doubleUpdatedY.ybounds shouldBe Bounds(-7, 7)
  }

}
