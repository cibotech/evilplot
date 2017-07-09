/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.geometry

import org.mockito.{InOrder, Mockito}
import org.scalajs.dom.CanvasRenderingContext2D
import org.scalatest.{FunSpec, Matchers}
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar

class GeometrySpec extends FunSpec with Matchers with MockitoSugar {

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

    it("Line draws") {
      val canvas = mock[CanvasRenderingContext2D]
      val line = Line(length, strokeWidth)
      line.draw(canvas)

      val order: InOrder = Mockito.inOrder(canvas)
      order.verify(canvas).beginPath
      order.verify(canvas).lineWidth_=(strokeWidth)
      order.verify(canvas).moveTo(0, strokeWidth / 2.0)
//TODO: add remaining calls, also cover the CanvasOp save/restore w shared code
    }

    it("Rect extent") {
      val extent = Rect(width, height).extent
      extent shouldEqual Extent(width, height)
    }

    it("Rect draws") {
      val canvas = mock[CanvasRenderingContext2D]
      val rect = Rect(width, height)
      rect.draw(canvas)

      verify(canvas).fillRect(0, 0, width, height)
    }

  }

}
