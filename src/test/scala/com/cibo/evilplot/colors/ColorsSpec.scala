/*
 * Copyright 2017 CiBO Technologies
 */

package com.cibo.evilplot.colors

import com.cibo.evilplot.colors.Colors.{ColorSeq, ScaledColorBar}
import org.scalatest.Matchers._
import org.scalatest._

class ColorsSpec extends FunSpec {
  describe("ScaledColorBar") {
    it("should assign distinct colors to different years") {
      val z: Seq[Double] = Seq(2012, 2013, 2012, 2011)

      val colorBar = ScaledColorBar(ColorSeq.getGradientSeq(3), z.min, z.max)
      val colorsFromData = z.map(colorBar.getColor)

      colorsFromData.head should === (colorsFromData(2))
      colorsFromData.head should !== (colorsFromData(1))
      colorsFromData.head should !== (colorsFromData(3))
    }
  }
}
