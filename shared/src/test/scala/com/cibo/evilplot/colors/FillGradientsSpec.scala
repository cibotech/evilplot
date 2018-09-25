package com.cibo.evilplot.colors

import com.cibo.evilplot.geometry.GradientFill
import org.scalatest.{FunSpec, Matchers}

class FillGradientsSpec extends FunSpec with Matchers {


  describe("Gradient distribution functions") {

    it("Should distribute 1 properly"){

      val gradientSeq = FillGradients.distributeEvenly(
        Seq.fill(1)(RGB.random)
      )

      gradientSeq(0).offset shouldEqual 0.0
      gradientSeq(1).offset shouldEqual 1.0
      gradientSeq(0).color shouldEqual gradientSeq.head.color
      gradientSeq(1).color shouldEqual gradientSeq.head.color

    }

    it("Should distribute 2 properly"){

      val gradientSeq = FillGradients.distributeEvenly(
        Seq(HTMLNamedColors.red, HTMLNamedColors.white)
      )

      gradientSeq(0).offset shouldEqual 0.0
      gradientSeq(0).color shouldEqual HTMLNamedColors.red

      gradientSeq(1).offset shouldEqual 1.0
      gradientSeq(1).color shouldEqual HTMLNamedColors.white

    }

    it("Should distribute 3 properly"){

      val gradientSeq = FillGradients.distributeEvenly(
        Seq(HTMLNamedColors.red, HTMLNamedColors.white, HTMLNamedColors.blue)
      )

      gradientSeq(0).offset shouldEqual 0.0
      gradientSeq(0).color shouldEqual HTMLNamedColors.red

      gradientSeq(1).offset shouldEqual 0.5
      gradientSeq(1).color shouldEqual HTMLNamedColors.white

      gradientSeq(2).offset shouldEqual 1.0
      gradientSeq(2).color shouldEqual HTMLNamedColors.blue

    }

    it("Should distribute 4 properly"){

      val gradientSeq = FillGradients.distributeEvenly(
        Seq(HTMLNamedColors.red, HTMLNamedColors.white, HTMLNamedColors.blue, HTMLNamedColors.mintCream)
      )

      gradientSeq(0).offset shouldEqual 0.0
      gradientSeq(0).color shouldEqual HTMLNamedColors.red

      gradientSeq(1).offset shouldEqual 0.333 +- 0.01
      gradientSeq(1).color shouldEqual HTMLNamedColors.white

      gradientSeq(2).offset shouldEqual 0.666 +- 0.01
      gradientSeq(2).color shouldEqual HTMLNamedColors.blue

      gradientSeq(3).offset shouldEqual 1.0
      gradientSeq(3).color shouldEqual HTMLNamedColors.mintCream

    }

  }
}
