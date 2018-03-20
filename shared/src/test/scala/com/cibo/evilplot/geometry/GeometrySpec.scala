/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.geometry

import org.scalatest.{FunSpec, Matchers}
import com.cibo.evilplot.DOMInitializer

class GeometrySpec extends FunSpec with Matchers {

  DOMInitializer.init()

  describe("Geometry") {

    // pick different values so that we can tell if they get swapped
    val width = 1.0
    val height = 2.0
    val length = 3.0
    val strokeWidth = 4.0

    it("Line extent") {
      val extent = Line(length, strokeWidth).extent
      extent shouldEqual Extent(length, strokeWidth)
    }

    it("Rect extent") {
      val extent = Rect(width, height).extent
      extent shouldEqual Extent(width, height)
    }

  }

  describe("labels") {
    class TestContext extends MockRenderContext {
      var discDrawn: Boolean = false
      var textDrawn: Boolean = false
      override def draw(disc: Disc): Unit = discDrawn = true
      override def draw(text: Text): Unit = textDrawn = true
      override def draw(translate: Translate): Unit = translate.r.draw(this)
    }

    it("titled should draw the drawable and text") {
      val context = new TestContext
      val d = Disc(5).titled("message")
      d.draw(context)
      context.discDrawn shouldBe true
      context.textDrawn shouldBe true
    }

    it("labeled should draw the drawable and text") {
      val context = new TestContext
      val d = Disc(5).labeled("message")
      d.draw(context)
      context.discDrawn shouldBe true
      context.textDrawn shouldBe true
    }
  }
}
