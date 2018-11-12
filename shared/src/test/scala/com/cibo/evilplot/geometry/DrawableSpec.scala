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

package com.cibo.evilplot.geometry

import com.cibo.evilplot.colors.{ColorGradients, FillGradients}
import org.scalatest.{FunSpec, Matchers}
import io.circe.syntax._
class DrawableSpec extends FunSpec with Matchers {

  import com.cibo.evilplot.plot.aesthetics.DefaultTheme._

  def encodeDeocde(before: Drawable) = {
    val str = Drawable.drawableEncoder(before).noSpaces
    val after = io.circe.parser.parse(str).right.get.as[Drawable].right.get
    after
  }

  describe("EmptyDrawable") {
    it("has zero size") {
      EmptyDrawable().extent shouldBe Extent(0, 0)
    }

    it("does nothing") {
      val context = new MockRenderContext
      EmptyDrawable().draw(context) // This should not throw an exception
    }

    it("can be serialized") {
      val before = EmptyDrawable()
      val str = Drawable.drawableEncoder(before).noSpaces
      val after = io.circe.parser.parse(str).right.get.as[Drawable].right.get
      after shouldBe before
    }
  }

  describe("Line") {
    val line = Line(5, 10)

    it("has the right extent") {
      line.extent shouldBe Extent(5, 10)
    }

    it("renders itself") {
      var drawn = 0
      val context = new MockRenderContext {
        override def draw(line: Line): Unit = drawn += 1
      }

      line.draw(context)
      drawn shouldBe 1
    }
  }

  describe("Disc") {
    it("has the right extent") {
      Disc(5).extent shouldBe Extent(10, 10)
    }
  }

  describe("Wedge") {
    it("has the right extent") {
      Wedge(180, 5).extent shouldBe Extent(10, 10)
    }
  }

  describe("extent"){
    it("can be serialized and deserialized"){

    }
  }

  describe("Interaction"){
    it("can be serialized and deserialized"){
      encodeDeocde(Interaction(Disc(10), EmptyEvent())) shouldEqual Interaction(Disc(10), EmptyEvent())
      encodeDeocde(Interaction(Disc(10), OnClick(_ => ()))) shouldEqual Interaction(Disc(10), EmptyEvent())

    }
  }

  describe("Gradient2d"){
    it("can be serialized and deserialized"){

      val gradient = LinearGradient.bottomToTop(Extent(100, 100),  FillGradients.distributeEvenly(ColorGradients.viridis))

      encodeDeocde(GradientFill(Rect(10), gradient)) shouldEqual GradientFill(Rect(10), gradient)
    }
  }
}
