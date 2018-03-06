package com.cibo.evilplot.geometry

import org.scalatest.{FunSpec, Matchers}

class DrawableSpec extends FunSpec with Matchers {

  import com.cibo.evilplot.plot.aesthetics.DefaultTheme._

  describe("EmptyDrawable") {
    it("has zero size") {
      EmptyDrawable().extent shouldBe Extent(0, 0)
    }

    it("does nothing") {
      val context = new MockRenderContext
      EmptyDrawable().draw(context)   // This should not throw an exception
    }

    it("can be serialized") {
      val before = EmptyDrawable()
      val str = Drawable.drawableEncoder(before).noSpaces
      val after = io.circe.parser.parse(str).right.get.as[Drawable].right.get
      after shouldBe before
    }
  }

  describe("Line") {
    val line = Line(5, 10)

    it("has the right extent") {
      line.extent shouldBe Extent(5, 10)
    }

    it("renders itself") {
      var drawn = 0
      val context = new MockRenderContext {
        override def draw(line: Line): Unit = drawn += 1
      }

      line.draw(context)
      drawn shouldBe 1
    }
  }
}
