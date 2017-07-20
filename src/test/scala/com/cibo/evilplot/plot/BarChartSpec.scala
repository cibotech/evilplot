/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.plot

import com.cibo.evilplot.colors.HSL
import com.cibo.evilplot.geometry.Extent
import com.cibo.evilplot.numeric._
import org.scalatest._


object DomInitializer {
  import org.scalajs.dom

  def init(): Unit = {
    var node = dom.document.createElement("CANVAS")
    node.setAttribute("id", "measureBuffer")
    dom.document.body.appendChild(node)
  }
}

// TODO: These tests need a lot of work before they are actually useful!
class BarChartSpec extends FunSpec with Matchers {

  DomInitializer.init()

  val chartSize = Extent(500, 400)
  val data = GaussianData.data
  val xBounds = (7.0, 13.0)
  val hist = new Histogram(data, 10)
  val graphData: Seq[Double] = hist.bins.map(_.toDouble)
   val bars1 = new Bars(chartSize, Some((data.min, data.max)), Some(xBounds),
     (graphData.min, graphData.max), graphData, HSL(0, 0, 0), barWidth = Some(10))
   val xAxis = new XAxis(bars1, xBounds._1, xBounds._2, 5)

  describe("BarChart") {
    it("should produce the correct number of bars when given a range tight around the bounds of the data") {
      bars1.nBars shouldEqual 11
    }

    it("should produce the correct number of grid lines") {
      val gridLines = new VerticalGridLines(xAxis, 1.0)
      gridLines.lines.length shouldEqual 6
    }
  }
}

