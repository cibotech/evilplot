package com.cibo.evilplot.geometry

import org.scalatest.{FunSpec, Matchers}
import com.cibo.evilplot.DOMInitializer

class TextMetricsSpec extends FunSpec with Matchers {
  DOMInitializer.init()

  // Compute how much we need to scale the width by since
  // the actual rendered text size can change depending on the
  // output device.
  private lazy val scale = TextMetrics.measure(Text("X", 10)).height / 10.0

  describe("measure") {
    it("returns the right size for a small font") {
      val extent = TextMetrics.measure(Text("test", 5))
      extent.width shouldBe 9.07471 * scale +- 0.5
      extent.height shouldBe 5.0 * scale +- 0.5
    }

    it("returns the right size for a large font") {
      val extent = TextMetrics.measure(Text("test", 64))
      extent.width shouldBe 116.15625 * scale +- 0.5
      extent.height shouldBe 64.0 * scale +- 0.5
    }
  }
}
