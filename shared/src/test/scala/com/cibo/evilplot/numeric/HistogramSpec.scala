/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.numeric

import org.scalatest._
import org.scalatest.Matchers._


class HistogramSpec extends FunSpec {

  describe("Histogram") {

    it("should handle a simple case properly") {
      // Set up the data so that the counts will be 1, 2, 3, and 4 respectively.
      // Arrange the data in non-ascending order to ensure that we're sorting.
      val offset = 3.3
      val data: Seq[Double] = Seq(
        0.50, 0.52, 0.74,
        0.0,
        0.75, 0.77, 0.78, 1.0,
        0.25, 0.49)
        // Add an offset to make sure that the histogram doesn't assume min = 0.0.
        .map(_ + offset)
      val numBins = 4
      val hist = Histogram(data, numBins)
      hist.min shouldEqual data.reduce[Double](math.min)
      hist.max shouldEqual data.reduce[Double](math.max)
      hist.binWidth shouldEqual (hist.max - hist.min) / numBins
      hist.bins shouldEqual List(1L, 2L, 3L, 4L)
    }


  }

}
