package com.cibo.evilplot.geometry

import java.awt.image.BufferedImage
import java.awt.{BasicStroke, Color, Graphics2D}

import org.scalatest.{FunSpec, Matchers}

class Graphics2DRenderContextSpec extends FunSpec with Matchers {
  describe("state stack operations") {
    it("should leave the state of the Graphics2D unmodified") {
      val graphics = Graphics2DTestUtils.graphics2D
      val ctx = Graphics2DRenderContext(graphics)
      val initialTransform = ctx.graphics.getTransform
      val initialFill = ctx.graphics.getPaint
      val initialColor = ctx.graphics.getColor
      val initialStroke = ctx.graphics.getStroke
      Graphics2DRenderContext.applyOp(ctx) {
        ctx.graphics.translate(34, 20)
        ctx.graphics.setPaint(Color.BLUE)
        ctx.graphics.setColor(Color.RED)
        ctx.graphics.setStroke(new BasicStroke(3))
      }
      ctx.graphics.getTransform shouldBe initialTransform
      ctx.graphics.getPaint shouldBe initialFill
      ctx.graphics.getColor shouldBe initialColor
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
