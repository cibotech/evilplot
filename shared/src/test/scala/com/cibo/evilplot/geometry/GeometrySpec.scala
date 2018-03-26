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

import org.scalatest.{FunSpec, Matchers}
import com.cibo.evilplot.DOMInitializer

class GeometrySpec extends FunSpec with Matchers {

  DOMInitializer.init()

  describe("Geometry") {

    // pick different values so that we can tell if they get swapped
    val width = 1.0
    val height = 2.0
    val length = 3.0
    val strokeWidth = 4.0

    it("Line extent") {
      val extent = Line(length, strokeWidth).extent
      extent shouldEqual Extent(length, strokeWidth)
    }

    it("Rect extent") {
      val extent = Rect(width, height).extent
      extent shouldEqual Extent(width, height)
    }

  }

  describe("labels") {
    class TestContext extends MockRenderContext {
      var discDrawn: Boolean = false
      var textDrawn: Boolean = false
      override def draw(disc: Disc): Unit = discDrawn = true
      override def draw(text: Text): Unit = textDrawn = true
      override def draw(translate: Translate): Unit = translate.r.draw(this)
    }

    it("titled should draw the drawable and text") {
      val context = new TestContext
      val d = Disc(5).titled("message")
      d.draw(context)
      context.discDrawn shouldBe true
      context.textDrawn shouldBe true
    }

    it("labeled should draw the drawable and text") {
      val context = new TestContext
      val d = Disc(5).labeled("message")
      d.draw(context)
      context.discDrawn shouldBe true
      context.textDrawn shouldBe true
    }
  }
}
