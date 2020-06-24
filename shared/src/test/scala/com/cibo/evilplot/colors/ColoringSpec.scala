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

package com.cibo.evilplot.colors

import com.cibo.evilplot.geometry.PointEquivalences
import com.cibo.evilplot.plot.aesthetics.DefaultTheme.{DefaultElements, DefaultFonts}
import com.cibo.evilplot.plot.aesthetics.{Colors, Elements, Fonts, Theme}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class ColoringSpec extends AnyFunSpec with Matchers {
  describe("multi color gradient construction") {
    import com.cibo.evilplot.plot.aesthetics.DefaultTheme._
    it("should return a function when Colors has only one element") {
      val min: Double = 0
      val max: Double = 100
      val coloring =
        GradientUtils.multiGradient(Seq(HTMLNamedColors.blue), min, max, GradientMode.Linear)
      Range.BigDecimal(min, max, 1.0)
        .map(_.toDouble)
        .foreach(datum => coloring(datum) shouldBe HTMLNamedColors.blue)
    }

    it("should throw an exception when Colors is empty") {
      an[IllegalArgumentException] shouldBe thrownBy(
        GradientUtils.multiGradient(Seq(), 0, 100, GradientMode.Linear))
    }

    it("should build multistop gradients") {
      import HTMLNamedColors.{red, yellow, green}
      val min = 0
      val max = 2
      val colors = Seq(red, yellow, green)
      val gradient = GradientUtils.multiGradient(colors, min, max, GradientMode.Linear)
      gradient(min) should ===(colors.head)
      gradient(1) should ===(colors(1))
      gradient(max) should ===(colors(2))
    }

    it("should return a function that works between min and max") {
      val data: Seq[Double] = Seq(0, 5, 20, 40, 70, 100)
      val gradient = ContinuousColoring.gradient(HTMLNamedColors.red, HTMLNamedColors.blue)

      val coloring = gradient(data)
      data.foreach(d => noException shouldBe thrownBy(coloring(d)))
    }

    it("should behave properly when asked to render past the edge") {
      val gradient = ContinuousColoring.gradient(HTMLNamedColors.red, HTMLNamedColors.blue)
      val coloring = gradient(Seq(1.0, 5.0))
      coloring(1.0) shouldBe HTMLNamedColors.red
      coloring(5.0) shouldBe HTMLNamedColors.blue
      coloring(6.0) shouldBe HTMLNamedColors.blue
    }
  }
  describe("coloring from the theme") {
    import com.cibo.evilplot.plot.aesthetics.DefaultTheme.{DefaultColors => AesColors}
    implicit val overriddenTheme: Theme = Theme(
      fonts = DefaultFonts,
      elements = DefaultElements,
      colors = AesColors.copy(stream = Seq(HTMLNamedColors.red))
    )
    it("should fail to color when the theme doesn't have enough colors") {
      val data = 0 to 5
      an[IllegalArgumentException] shouldBe thrownBy(CategoricalColoring.themed[Int].apply(data))
    }
  }
  describe("making a coloring out of a custom mapping") {
    import com.cibo.evilplot.plot.aesthetics.DefaultTheme._
    it("should actually use the mapping") {
      val f = (s: String) => if (s == "hello") HTMLNamedColors.blue else HTMLNamedColors.red
      val coloring = CategoricalColoring.fromFunction(Seq("hello", "world"), f)
      val extractedFunc = coloring(Seq("hello", "world"))
      extractedFunc("hello") shouldBe HTMLNamedColors.blue
      extractedFunc("world") shouldBe HTMLNamedColors.red
    }
  }
}
