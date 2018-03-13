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
      val ticks = ContinuousAxisDescriptor(Bounds(-3500, 6100), 5, fixed = false)
      ticks.tickMin shouldEqual -4000
      ticks.tickMax shouldEqual 8000
      ticks.spacing shouldEqual 2000
      ticks.numFrac shouldEqual 0
    }

    it("doesn't update bounds when fixed") {
      val ticks = ContinuousAxisDescriptor(Bounds(-3500, 6100), 5, fixed = true)
      ticks.tickMin shouldBe -3500
      ticks.tickMax shouldBe 6100
      ticks.spacing shouldBe 2000
    }

    it("gives nice ticks, with small numbers") {
      val ticks = ContinuousAxisDescriptor(Bounds(-0.01, 0.07), 3, fixed = false)
      ticks.tickMin shouldEqual -0.02
      ticks.tickMax shouldEqual 0.08
      ticks.spacing shouldEqual 0.02
      ticks.numFrac shouldEqual 2
    }

    it("does not fail when the min and max are the same within machine precision") {
      val ticks = ContinuousAxisDescriptor(Bounds(0.5, 0.5), 10, fixed = false)
      ticks.tickMin shouldBe 0.0 +- AxisDescriptor.machineEpsilonIEEE754Double
      ticks.tickMax shouldBe 1.0 +- AxisDescriptor.machineEpsilonIEEE754Double
      println(ticks.numFrac)
    }

    it("does not fail when the axisBounds evaluate to Bounds(NaN, NaN)") {
      val ticks = ContinuousAxisDescriptor(Bounds(Double.NaN, Double.NaN), 10, fixed = false)
      ticks.tickMin.isNaN shouldBe true
      ticks.tickMax.isNaN shouldBe true
      ticks.numFrac shouldBe 0
    }
  }

}
