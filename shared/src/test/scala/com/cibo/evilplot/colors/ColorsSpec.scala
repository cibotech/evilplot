/*
 * Copyright 2017 CiBO Technologies
 */

package com.cibo.evilplot.colors

import org.scalatest.Matchers._
import org.scalatest._

class ColorsSpec extends FunSpec {
  describe("ScaledColorBar") {
    it("should assign distinct colors to different years") {
      val z: Seq[Double] = Seq(2012, 2013, 2012, 2011)

      val colorBar = ScaledColorBar(Color.getGradientSeq(3), z.min, z.max)
      val colorsFromData = z.map(colorBar.getColor)

      colorsFromData.head should === (colorsFromData(2))
      colorsFromData.head should !== (colorsFromData(1))
      colorsFromData.head should !== (colorsFromData(3))
    }
  }

  describe("HSLA functions") {
    it("should darken and lighten") {
      val baseColor = HSLA(0, 0, 0, 0)

      baseColor.lighten(50).lightness shouldEqual 50
      baseColor.darken(50).lightness shouldEqual 0

      baseColor.lighten(100).darken(50).lightness shouldEqual 50
    }
  }

  describe("Color Utils"){
    it("should convert rgba to hsla correctly"){
      RGB(50, 50, 50) shouldEqual HSLA(0, 0, 20, 1.0)
      RGB(50, 50, 32) shouldEqual HSLA(60, 22, 16, 1.0)
      RGB(255, 255, 255) shouldEqual HSLA(0, 0, 100, 1.0)
      RGBA(255, 255, 255, 0.5) shouldEqual HSLA(0, 0, 100, 0.5)

    }

    it("should throw exceptions with bad rgba"){
      an[IllegalArgumentException] should be thrownBy RGBA(0, 500, 0, 1.0)
      an[IllegalArgumentException] should be thrownBy RGBA(0, 0, 0, 2.0)

    }

    it("should convert hex to hsla correctly"){
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

    it("should throw exceptions with bad hexes"){
      an[IllegalArgumentException] should be thrownBy HEX("")
      an[IllegalArgumentException] should be thrownBy HEX("FFFFFFF")
      an[IllegalArgumentException] should be thrownBy HEX("FFFFFFFFF")

    }

  }
}
