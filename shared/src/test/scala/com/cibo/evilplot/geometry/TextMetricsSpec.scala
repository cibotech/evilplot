package com.cibo.evilplot.geometry

import org.scalatest.{FunSpec, Matchers}
import com.cibo.evilplot.DOMInitializer

class TextMetricsSpec extends FunSpec with Matchers {
  DOMInitializer.init()

  describe("measure") {
    it("returns the right size for a small font") {
      val extent = TextMetrics.measure(Text("test", 5))
      extent.width shouldBe 9.0 +- 0.1
      extent.height shouldBe 5.0 +- 0.1
    }

    it("returns the right size for a large font") {
      val extent = TextMetrics.measure(Text("test", 64))
      extent.width shouldBe 116.1 +- 0.1
      extent.height shouldBe 64.0 +- 0.1
    }
  }
}
