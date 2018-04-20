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

import java.awt.image.BufferedImage
import java.awt.{BasicStroke, Color, Graphics2D}

import org.scalatest.{FunSpec, Matchers}

class Graphics2DRenderContextSpec extends FunSpec with Matchers with Graphics2DSupport {
  describe("state stack operations") {
    it("The state should be the same before and after a stack op.") {
      val graphics = Graphics2DTestUtils.graphics2D
      val ctx = Graphics2DRenderContext(graphics)
      val GraphicsState(initialTransform, initialFill, initialColor, initialStroke) =
        ctx.initialState
      Graphics2DRenderContext.applyOp(ctx) {
        ctx.graphics.translate(34, 20)
        ctx.graphics.setPaint(Color.BLUE)
        ctx.graphics.setStroke(new BasicStroke(3))
      }
      ctx.graphics.getTransform shouldBe initialTransform
      ctx.fillColor shouldBe initialFill
      ctx.strokeColor shouldBe initialColor
      ctx.graphics.getStroke shouldBe initialStroke
    }
  }
}

object Graphics2DTestUtils {
  def graphics2D: Graphics2D = {
    val bi = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB)
    bi.createGraphics()
  }
}
