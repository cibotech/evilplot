/*
 * Copyright 2017 CiBO Technologies
 */

package com.cibo.evilplot.numeric

import com.cibo.evilplot.plot.Bounds
import org.scalatest._
import org.scalatest.Matchers._


class TicksSpec extends FunSpec {

  describe("Ticks") {

    it("returns a nice number, with rounding") {
      (1 to 10).map(num => Ticks.nicenum(num, round = true)) shouldEqual
        Seq(1.0, 2.0, 5.0, 5.0, 5.0, 5.0, 10.0, 10.0, 10.0, 10.0)
    }

    it("returns a nice number, with ceiling") {
      (1 to 10).map(num => Ticks.nicenum(num, round = false)) shouldEqual
        Seq(1.0, 2.0, 5.0, 5.0, 5.0, 10.0, 10.0, 10.0, 10.0, 10.0)
    }

    it("gives nice ticks, spanning 0") {
      val ticks = Ticks(Bounds(-3500, 6100), 5)
      ticks.tickMin shouldEqual -2000
      ticks.tickMax shouldEqual 6000
      ticks.spacing shouldEqual 2000
      ticks.numFrac shouldEqual 0
    }

    it("gives nice ticks, with small numbers") {
      val ticks = Ticks(Bounds(-0.01, 0.07), 3)
      ticks.tickMin shouldEqual 0
      ticks.tickMax shouldEqual 0.06
      ticks.spacing shouldEqual 0.02
      ticks.numFrac shouldEqual 2
    }

  }

}
