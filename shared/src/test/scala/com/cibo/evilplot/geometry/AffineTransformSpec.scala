package com.cibo.evilplot.geometry

import org.scalatest.{FunSpec, Matchers}

class AffineTransformSpec extends FunSpec with Matchers {

  import com.cibo.evilplot.plot.aesthetics.DefaultTheme._

  describe("The AffineTransform") {
    it("should translate a point") {
      AffineTransform.identity.translate(1.0, 0.0)(1.0, 1.0) should be ((2.0, 1.0))
      AffineTransform.identity.translate(0.0, 1.0)(1.0, 1.0) should be ((1.0, 2.0))
    }

    it("should scale a point") {
      AffineTransform.identity.scale(2.0, 1.0)(1.0, 1.0) should be ((2.0, 1.0))
      AffineTransform.identity.scale(1.0, 2.0)(1.0, 1.0) should be ((1.0, 2.0))
    }

    it("should flip a point across the axes") {
      AffineTransform.identity.flipOverX(0.0, 1.0) should be ((0.0, -1.0))
      AffineTransform.identity.flipOverY(1.0, 0.0) should be ((-1.0, 0.0))
    }

    it("should rotate by 90 degrees") {
      val (x, y) = AffineTransform.identity.rotateDegrees(90)(1.0, 0.0)
      x should be (0.0 +- 1e-9)
      y should be (1.0 +- 1e-9)
    }

    it("should compose two affine transforms") {
      val translate = AffineTransform.identity.translate(1.0, 0.0)
      val scale = AffineTransform.identity.scale(1.0, 2.0)

      translate.compose(scale)(2.0, 3.0) should be ((3.0, 6.0))
      scale.compose(translate)(2.0, 3.0) should be ((3.0, 6.0))
    }
  }
}
