/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.plot

import com.cibo.evilplot.colors
import com.cibo.evilplot.geometry.Extent
import com.cibo.evilplot.numeric.{Bounds, Point}
import org.scalatest.{FunSpec, Matchers}

class LinePlotSpec extends FunSpec with Matchers {

  describe("class LineToPlot") {
    val points = Seq(Point(1, 3), Point(3, 4), Point(0, 2), Point(2, 5))
    val line = OneLinePlotData(points, colors.Blue)

    it("has the right xBounds") {
      line.xBounds shouldEqual Bounds(0, 3)
    }

    it("has the right yBounds") {
      line.yBounds shouldEqual Bounds(2, 5)
    }
  }

  describe("object LineToPlot") {
    val points1 = Seq(Point(1, -2), Point(7, 4), Point(0, 2), Point(2, 5))
    val line1 = OneLinePlotData(points1, colors.Blue)

    val points2 = Seq(Point(1, 6), Point(3, 4), Point(0, 2), Point(-1, 5))
    val line2 = OneLinePlotData(points2, colors.Gold)

    val lines = Seq(line1, line2)

    it("has the right xBounds") {
      LinePlotData(lines).xBounds shouldEqual Some(Bounds(-1, 7))
    }

    it("has the right yBounds") {
      LinePlotData(lines).yBounds shouldEqual Some(Bounds(-2, 6))
    }
  }

  // This test is very weak because all we get back from LinesLater is a Drawable, no way to inspect it.
  // We could do much more testing given a scene graph, but that would be a lot of work.
  describe("LinesLater") {
    val lines = Seq(
      OneLinePlotData(Seq(Point(1, 2)), colors.Oldlace),
      OneLinePlotData(Seq(Point(3, 4)), colors.Burlywood))
  }

}
