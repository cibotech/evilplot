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

package com.cibo.evilplot.plot.components

import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent, Group, Rect, Translate}
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.{CartesianPlot, Plot}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class ComponentGroupSpec extends AnyFunSpec with Matchers {

  import com.cibo.evilplot.plot.aesthetics.DefaultTheme._

  abstract class MockComponent(val position: Position) extends FacetedPlotComponent
  abstract class LeftComponent extends MockComponent(Position.Left)
  abstract class TopComponent extends MockComponent(Position.Top)
  abstract class RightComponent extends MockComponent(Position.Right)
  abstract class BottomComponent extends MockComponent(Position.Bottom)
  abstract class OverlayComponent extends MockComponent(Position.Overlay)
  abstract class BackgroundComponent extends MockComponent(Position.Background)

  describe("ComponentGroup") {

    val rectFore = Rect(50)
    val rectMiddle = Rect(120)
    val rectBack = Rect(35)
    val emptyPlot = CartesianPlot(Seq.empty)()

    def simpleMockComponent(
      componentPosition: Position,
      drawable: Drawable,
      expectedPlot: Option[Plot] = None,
      expectedExtent: Option[Extent] = None
    ): FacetedPlotComponent = {
      val whateverSize = Extent(999, 999)
      val whateverPlot = emptyPlot

      new FacetedPlotComponent {
        val position: Position = componentPosition

        override def size(plot: Plot): Extent = drawable.extent

        def render(plot: Plot, extent: Extent, row: Int, column: Int)(
          implicit theme: Theme): Drawable =
          drawable
      }
    }

    it("returns an EmptyDrawable when it has zero components") {
      val cg = ComponentGroup(Position.Top)

      cg.size(emptyPlot) shouldBe Extent(0, 0)
      cg.render(emptyPlot, Extent(0, 0), 0, 0) shouldBe EmptyDrawable()
    }

    it("requires all components to have the same position") {
      an[IllegalArgumentException] should be thrownBy {
        ComponentGroup(
          Position.Top,
          Seq(
            simpleMockComponent(Position.Top, EmptyDrawable()),
            simpleMockComponent(Position.Overlay, EmptyDrawable())
          ))
      }
    }

    it("renders components in the correct order") {
      val emptyExtent = Extent(0, 0)
      val expectedDrawable = Group(Seq(rectBack, rectMiddle, rectFore))

      val cg = ComponentGroup(
        Position.Right,
        Seq(
          simpleMockComponent(Position.Right, rectFore, Some(emptyPlot), Some(emptyExtent)),
          simpleMockComponent(Position.Right, rectMiddle, Some(emptyPlot), Some(emptyExtent)),
          simpleMockComponent(Position.Right, rectBack, Some(emptyPlot), Some(emptyExtent))
        )
      )

      cg.size(emptyPlot) shouldBe Extent(120, 120)
      cg.render(emptyPlot, Extent(0, 0), 0, 0) shouldBe expectedDrawable
    }

    it("returns the correct size width for left and right components") {
      val cgLeft = ComponentGroup(
        Position.Left,
        Seq(
          simpleMockComponent(Position.Left, Rect(71, 20)),
          simpleMockComponent(Position.Left, Rect(30, 20))
        ))

      val cgRight = ComponentGroup(
        Position.Right,
        Seq(
          simpleMockComponent(Position.Right, Rect(71, 20)),
          simpleMockComponent(Position.Right, Rect(30, 20))
        ))

      cgLeft.size(emptyPlot).width shouldBe 71
      cgRight.size(emptyPlot).width shouldBe 71
    }

    it("returns the correct size height for top and bottom components") {
      val cgTop = ComponentGroup(
        Position.Top,
        Seq(
          simpleMockComponent(Position.Top, Rect(10, 20)),
          simpleMockComponent(Position.Top, Rect(10, 45))
        ))

      val cgBottom = ComponentGroup(
        Position.Bottom,
        Seq(
          simpleMockComponent(Position.Bottom, Rect(10, 20)),
          simpleMockComponent(Position.Bottom, Rect(10, 45))
        ))

      cgTop.size(emptyPlot).height shouldBe 45
      cgBottom.size(emptyPlot).height shouldBe 45
    }

    it("bottom aligns top components") {
      val expectedDrawable =
        Group(Seq(Translate(rectBack, y = 85), rectMiddle, Translate(rectFore, y = 70)))

      val cg = ComponentGroup(
        Position.Top,
        Seq(
          simpleMockComponent(Position.Top, rectFore),
          simpleMockComponent(Position.Top, rectMiddle),
          simpleMockComponent(Position.Top, rectBack)
        ))

      cg.size(emptyPlot) shouldBe Extent(120, 120)
      cg.render(emptyPlot, Extent(0, 0), 0, 0) shouldBe expectedDrawable
    }

    it("right aligns left components") {
      val expectedDrawable =
        Group(Seq(Translate(rectBack, x = 85), rectMiddle, Translate(rectFore, x = 70)))

      val cg = ComponentGroup(
        Position.Left,
        Seq(
          simpleMockComponent(Position.Left, rectFore),
          simpleMockComponent(Position.Left, rectMiddle),
          simpleMockComponent(Position.Left, rectBack)
        )
      )

      cg.size(emptyPlot) shouldBe Extent(120, 120)
      cg.render(emptyPlot, Extent(0, 0), 0, 0) shouldBe expectedDrawable
    }

  }

}
