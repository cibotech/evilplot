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

import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class ColorsSpec extends AnyFunSpec with TypeCheckedTripleEquals with Matchers {
  describe("ScaledColorBar") {
    it("should assign distinct colors to different years") {
      val z: Seq[Double] = Seq(2012, 2013, 2012, 2011)

      val colorBar = ScaledColorBar(Color.getGradientSeq(3), z.min, z.max)
      val colorsFromData = z.map(colorBar.getColor)

      colorsFromData.head should ===(colorsFromData(2))
      colorsFromData.head should !==(colorsFromData(1))
      colorsFromData.head should !==(colorsFromData(3))
    }

    it("should clip data to the colorbar's range") {
      val z: Seq[Double] = Seq(2012, 2013, 2012, 2011, 2014, 2010)

      val colorBar = ScaledColorBar(Color.getGradientSeq(3), 2011, 2013)
      val colorsFromData = z.map(colorBar.getColor)

      colorsFromData.head should ===(colorsFromData(2))
      colorsFromData.head should !==(colorsFromData(1))
      colorsFromData.head should !==(colorsFromData(3))
      colorsFromData(3) should ===(colorsFromData(5))
      colorsFromData(1) should ===(colorsFromData(4))
    }
  }

  describe("toRGBA cfunctions") {
    it("should make clear transparent") {
      val baseColor = Clear
      baseColor.rgba._4 shouldBe 0.0
    }
    it("should recover a rgba color") {
      val baseColor = RGBA(77, 255, 22, 1.0)

      baseColor.rgba shouldBe (75, 255, 20, 1.0)
    }
  }

  describe("HSLA functions") {
    it("should darken and lighten") {
      val baseColor = HSLA(0, 0, 0, 0)

      baseColor.lighten(50).lightness shouldEqual 50
      baseColor.darken(50).lightness shouldEqual 0

      baseColor.lighten(100).darken(50).lightness shouldEqual 50
    }

    describe("triadic") {
      it("correctly bounds hues that would be exactly 360") {
        val color = HSLA(240, 0, 0, 0)

        color.triadic shouldEqual (HSLA(120, 0, 0, 0), HSLA(0, 0, 0, 0))
      }
    }

    describe("analogous") {
      it("correctly bounds hues that would be exactly 360") {
        val color = HSLA(350, 0, 0, 0)

        color.analogous(10) shouldEqual (HSLA(340, 0, 0, 0), HSLA(0, 0, 0, 0))
      }
    }
  }

  describe("Color Utils") {
    it("should convert rgba to hsla correctly") {
      RGB(50, 50, 50) shouldEqual HSLA(0, 0, 20, 1.0)
      RGB(50, 50, 32) shouldEqual HSLA(60, 22, 16, 1.0)
      RGB(255, 255, 255) shouldEqual HSLA(0, 0, 100, 1.0)
      RGBA(255, 255, 255, 0.5) shouldEqual HSLA(0, 0, 100, 0.5)

    }

    it("should throw exceptions with bad rgba") {
      an[IllegalArgumentException] should be thrownBy RGBA(0, 500, 0, 1.0)
      an[IllegalArgumentException] should be thrownBy RGBA(0, 0, 0, 2.0)

    }

    it("should convert hex to hsla correctly") {
      HEX("#e58080") shouldEqual HSLA(0, 66, 70, 1.0)
      HEX("#ff0000") shouldEqual HSLA(0, 100, 50, 1.0)
      HEX("#00ff00") shouldEqual HSLA(120, 100, 50, 1.0)
      HEX("#0000ff") shouldEqual HSLA(240, 100, 50, 1.0)

      HEX("#0000FF") shouldEqual HSLA(240, 100, 50, 1.0)

      HEX("#fff") shouldEqual ColorUtils.hexToHsla("#ffffff")
      HEX("#fff") shouldEqual HSLA(0, 0, 100, 1.0)
      HEX("#aaa") shouldEqual HSLA(0, 0, 67, 1.0)
      HEX("#000") shouldEqual HSLA(0, 0, 0, 1.0)

      HEX("#000000") shouldEqual HSLA(0, 0, 0, 1.0)
      HEX("#FFFFFF") shouldEqual HSLA(0, 0, 100, 1.0)

      HEX("000000") shouldEqual HSLA(0, 0, 0, 1.0)
      HEX("FFFFFF") shouldEqual HSLA(0, 0, 100, 1.0)

    }

    it("should not fail when rounding sends hue to 360") {
      RGB(252, 0, 2) shouldBe HSLA(0, 100, 49, 1.0)
    }

    it("should throw exceptions with bad hexes") {
      an[IllegalArgumentException] should be thrownBy HEX("")
      an[IllegalArgumentException] should be thrownBy HEX("FFFFFFF")
      an[IllegalArgumentException] should be thrownBy HEX("FFFFFFFFF")
    }

    it("should convert HSLA to RGBA") {
      ColorUtils.hslaToRgba(HSLA(0, 100, 50, 0.5)) shouldBe (1.0, 0, 0, 0.5)
      ColorUtils.hslaToRgba(HSL(25, 75, 47)) shouldBe (.8225, .41125, .1175, 1.0)
    }
  }
}
