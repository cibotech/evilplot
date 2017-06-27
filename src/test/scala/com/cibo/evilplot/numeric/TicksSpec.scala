/*
 * Copyright 2017 CiBO Technologies
 */

package com.cibo.evilplot.numeric

import org.scalatest._
import org.scalatest.Matchers._


class TicksSpec extends FunSpec {

  describe("Ticks") {

    it("returns a nice number, with rounding") {
      (1 to 10).map(num => Ticks.nicenum(num, true)) shouldEqual
        Seq(1.0, 2.0, 5.0, 5.0, 5.0, 5.0, 10.0, 10.0, 10.0, 10.0)
    }

    it("returns a nice number, with ceiling") {
      (1 to 10).map(num => Ticks.nicenum(num, false)) shouldEqual
        Seq(1.0, 2.0, 5.0, 5.0, 5.0, 10.0, 10.0, 10.0, 10.0, 10.0)
    }

    it("gives nice ticks, spanning 0") {
      val (tickMin, tickMax, spacing, numFrac) = Ticks.niceTicks(-3500, 6100, 5)
      tickMin shouldEqual -2000
      tickMax shouldEqual 6000
      spacing shouldEqual 2000
      numFrac shouldEqual 0
    }

    it("gives nice ticks, with small numbers") {
      val (tickMin, tickMax, spacing, numFrac) = Ticks.niceTicks(-0.01, 0.07, 3)
      println (tickMin, spacing, numFrac)
      tickMin shouldEqual 0
      tickMax shouldEqual 0.06
      spacing shouldEqual 0.02
      numFrac shouldEqual 2
    }

  }

}
