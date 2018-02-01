/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.geometry

import org.scalatest.{FunSpec, Matchers}


class GeometrySpec extends FunSpec with Matchers {

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

}
