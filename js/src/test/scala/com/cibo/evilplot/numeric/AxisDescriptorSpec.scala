/*
 * Copyright 2017 CiBO Technologies
 */

package com.cibo.evilplot.numeric

import org.scalatest.Matchers._
import org.scalatest._


class AxisDescriptorSpec extends FunSpec {

  describe("Ticks") {

    it("returns a nice number, with rounding") {
      (1 to 10).map(num => AxisDescriptor.nicenum(num, round = true)) shouldEqual
        Seq(1.0, 2.0, 5.0, 5.0, 5.0, 5.0, 10.0, 10.0, 10.0, 10.0)
    }

    it("returns a nice number, with ceiling") {
      (1 to 10).map(num => AxisDescriptor.nicenum(num, round = false)) shouldEqual
        Seq(1.0, 2.0, 5.0, 5.0, 5.0, 10.0, 10.0, 10.0, 10.0, 10.0)
    }

    it("gives nice ticks, spanning 0") {
      val ticks = AxisDescriptor(Bounds(-3500, 6100), 5)
      ticks.tickMin shouldEqual -4000
      ticks.tickMax shouldEqual 8000
      ticks.spacing shouldEqual 2000
      ticks.numFrac shouldEqual 0
    }

    it("gives nice ticks, with small numbers") {
      val ticks = AxisDescriptor(Bounds(-0.01, 0.07), 3)
      ticks.tickMin shouldEqual -0.02
      ticks.tickMax shouldEqual 0.08
      ticks.spacing shouldEqual 0.02
      ticks.numFrac shouldEqual 2
    }

  }

}
