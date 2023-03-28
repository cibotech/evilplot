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

package com.cibo.evilplot.geometry

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class AffineTransformSpec extends AnyFunSpec with Matchers {

  import com.cibo.evilplot.plot.aesthetics.DefaultTheme._

  describe("The AffineTransform") {
    it("should translate a point") {
      AffineTransform.identity.translate(1.0, 0.0)(1.0, 1.0) should be((2.0, 1.0))
      AffineTransform.identity.translate(0.0, 1.0)(1.0, 1.0) should be((1.0, 2.0))
    }

    it("should scale a point") {
      AffineTransform.identity.scale(2.0, 1.0)(1.0, 1.0) should be((2.0, 1.0))
      AffineTransform.identity.scale(1.0, 2.0)(1.0, 1.0) should be((1.0, 2.0))
    }

    it("should flip a point across the axes") {
      AffineTransform.identity.flipOverX(0.0, 1.0) should be((0.0, -1.0))
      AffineTransform.identity.flipOverY(1.0, 0.0) should be((-1.0, 0.0))
    }

    it("should rotate by 90 degrees") {
      val (x, y) = AffineTransform.identity.rotateDegrees(90)(1.0, 0.0)
      x should be(0.0 +- 1e-9)
      y should be(1.0 +- 1e-9)
    }

    it("should compose two affine transforms") {
      val translate = AffineTransform.identity.translate(1.0, 0.0)
      val scale = AffineTransform.identity.scale(1.0, 2.0)

      translate.compose(scale)(2.0, 3.0) should be((3.0, 6.0))
      scale.compose(translate)(2.0, 3.0) should be((3.0, 6.0))
    }
  }
}
