package com.cibo.evilplot.numeric

import org.scalatest.{FunSpec, Matchers}

class ScalingSpec extends FunSpec with Matchers {

  describe("LinearScaling") {

    it("scales correctly") {
      val scale = LinearScaling((4, 8), (100, 200))
      scale.scale(4) shouldEqual 100
      scale.scale(8) shouldEqual 200
      scale.scale(6) shouldEqual 150
      scale.scale(2) shouldEqual 50
      scale.scale(10) shouldEqual 250
    }

  }

}
