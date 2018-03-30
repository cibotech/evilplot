package com.cibo.evilplot.geometry

import java.awt.image.BufferedImage
import java.awt.{BasicStroke, Color, Graphics2D}

import org.scalatest.{FunSpec, Matchers}

class Graphics2DRenderContextSpec
    extends FunSpec
    with Matchers
    with Graphics2DSupport {
  describe("state stack operations") {
    it("The state should be the same before and after a stack op.") {
      val graphics = Graphics2DTestUtils.graphics2D
      val ctx = Graphics2DRenderContext(graphics)
      val GraphicsState(initialTransform, initialFill, initialColor, initialStroke) = ctx.initialState
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
