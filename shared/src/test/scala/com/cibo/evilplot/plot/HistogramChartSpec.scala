/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.plot

import com.cibo.evilplot.colors.HTMLNamedColors
import com.cibo.evilplot.geometry.Extent
import com.cibo.evilplot.numeric._
import com.cibo.evilplot.DOMInitializer
import org.scalatest._

import scala.util.Random

// TODO: These tests need a lot of work before they are actually useful!
class HistogramChartSpec extends FunSpec with Matchers {

  DOMInitializer.init()
  val chartSize = Extent(500, 400)

  describe("HistogramChart") {
    it("should produce the correct number of bars when given a range tight around the bounds of the data") {
      val data = GaussianData.data
      val xBounds = Bounds(7.0, 13.0)
      val hist = Histogram(data, 10)
      val graphData: Seq[Double] = hist.bins.map(_.toDouble)
      val bars = Bars(Extent(5, 5), xBounds, None, Bounds(data.min, data.max), graphData, HTMLNamedColors.black)
      bars.heightsToDraw.length shouldBe graphData.length
      // Now the number of bars is wrapped inside an apply method, so need to think yet again about how to access that.
    }

    it("should handle including extra / excluding bars correctly") {
      // 150 doubles on (0, 25)
      val data: Seq[Double] = Seq.fill(150)(25 * Random.nextDouble)
      // 5 on interval 0 to 25
      val histBounds = Bounds(0, 25)
      val drawBounds = Bounds(-5, 20)
      val hist = Histogram(data, 5, bounds = Some(histBounds))
      val graphData = hist.bins.map(_.toDouble)
      val bars = Bars(Extent(5, 5), histBounds, Some(drawBounds), Bounds(data.min, data.max), graphData,
        HTMLNamedColors.black)
      // should add 1 bar on left, drop one on right
      bars.heightsToDraw.tail.length shouldEqual graphData.init.length
      (bars.heightsToDraw.tail zip graphData.init).foreach {
        case (adjusted, original) => adjusted shouldEqual original
      }
    }
  }
}
