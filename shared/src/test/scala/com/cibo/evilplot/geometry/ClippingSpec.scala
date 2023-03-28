/*
 * Copyright (c) 2018, CiBO Technologies, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.cibo.evilplot.geometry

import com.cibo.evilplot.geometry.Clipping.Edge
import com.cibo.evilplot.numeric.{Point, Point2d}
import org.scalactic.{Equality, TolerantNumerics}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

trait PointEquivalences {
  implicit object PointEquivalence extends Equality[Point2d] {
    import math.{abs}
    def areEqual(a: Point2d, b: Any): Boolean = b match {
      case Point(x, y) =>
        val tol = 1e-5
        abs(a.x - x) < tol && abs(a.y - y) < tol
      case _ => false
    }
  }

  implicit object SeqPointEquivalence extends Equality[Seq[Point2d]] {
    val eq = implicitly[Equality[Point2d]]
    def areEqual(a: Seq[Point2d], b: Any): Boolean = b match {
      case bx: Seq[_] => a.corresponds(bx)((i, j) => eq.areEqual(i, j))
      case _          => false
    }
  }

  implicit object SeqSeqPointEquivalence extends Equality[Seq[Seq[Point2d]]] {
    val eq = implicitly[Equality[Seq[Point2d]]]

    def areEqual(a: Seq[Seq[Point2d]], b: Any): Boolean = b match {
      case bx: Vector[_] => a.corresponds(bx)((i, j) => eq.areEqual(i, j))
      case _             => false
    }
  }
}

object PointEquivalences extends PointEquivalences

class ClippingSpec extends AnyFunSpec with Matchers with PointEquivalences {

  describe("Edge") {
    it("vertical edge intersections are calculated correctly") {
      val interesection = Edge(Point(2, 2), Point(2, 0))
        .intersection(Edge(Point(1.5, 3), Point(2.1, 1)))

      interesection.head.x shouldEqual 2.0 +- 0.0000001
      interesection.head.y shouldEqual 1.3333 +- 0.0001
    }

    it("vertical line intersections are calculated correctly") {
      val interesection = Edge(Point(1.5, 3), Point(2.1, 1))
        .intersection(Edge(Point(2, 2), Point(2, 0)))

      interesection.head.x shouldEqual 2.0 +- 0.0000001
      interesection.head.y shouldEqual 1.3333 +- 0.0001
    }
  }

  describe("Path clipping") {
    it("handles an empty path") {
      Clipping.clipPath(Seq.empty, Extent(2, 2)) shouldBe Seq.empty[Seq[Point]]
    }

    it("removes segments entirely outside bounds") {
      Clipping.clipPath(Seq(Point(0, 4), Point(1.5, 6)), Extent(2, 2)) shouldBe Seq
        .empty[Seq[Point]]
    }

    it("should properly clip a line across a bound") {
      Clipping.clipPath(Seq(Point(0, 1), Point(1.5, 3)), Extent(2, 2)) shouldBe Seq(
        Seq(Point(0, 1), Point(0.75, 2)))
    }

    it("should properly clip a line with both points outside the bounds") {
      Clipping.clipPath(
        Seq(Point(-3, -3), Point(1.5, 3)),
        Extent(2, 2)
      ) shouldBe Seq(Seq(Point(0, 1), Point(0.75, 2)))
    }

    it("segments a path that crosses bounds multiple times") {
      val path = Seq(
        Point(0, 1),
        Point(1.5, 3),
        Point(2.1, 1),
        Point(1.5, 0.5),
        Point(1, 1)
      )
      val expected = Vector(
        Vector(
          Point(0, 1),
          Point(0.75, 2)
        ),
        Vector(
          Point(1.8, 2),
          Point(2, 4 / 3d)
        ),
        Vector(
          Point(2, 5.5 / 6d),
          Point(1.5, 0.5),
          Point(1, 1)
        )
      )
      Clipping.clipPath(path, Extent(2, 2)).shouldEqual(expected)(SeqSeqPointEquivalence)
    }
  }

  describe("Polygon clipping") {
    it("clips a line segment") {
      val expected = Seq(Point(0, 1), Point(0.75, 2))
      Clipping.clipPolygon(Seq(Point(0, 1), Point(1.5, 3)), Extent(2, 2)) should contain allElementsOf expected
    }

    it("clips a triangle") {
      val triangle = Seq(
        Point(0, 1),
        Point(1.5, 3),
        Point(2, 0.5)
      )
      val expected = Seq(
        Point(0, 1),
        Point(0.75, 2),
        Point(1.7, 2),
        Point(2, 0.5)
      )

      val clipped = Clipping.clipPolygon(triangle, Extent(2, 2))
      clipped shouldBe expected
      clipped should have length expected.length
    }

    it("clips a polygon that exits at the corner of the bounding box") {
      val polygon = Seq(Point(5, 5), Point(5, -5), Point(15, -5), Point(15, 5))
      val expected = Seq(Point(5, 5), Point(5, 0), Point(10, 0), Point(10, 5))
      val clipped = Clipping.clipPolygon(polygon, Extent(10, 10))
      clipped should contain theSameElementsAs expected
      clipped should have length polygon.length
    }

    it("should return the polygon when it is entirely inside the clipping region") {
      val polygon = Seq(Point(5, 5), Point(5, 0), Point(10, 0), Point(10, 5))
      val clipped = Clipping.clipPolygon(polygon, Extent(10, 10))
      clipped should contain theSameElementsAs polygon
      clipped should have length polygon.length
    }

    it("should return the clipping region when the polygon entirely encloses it") {
      val polygon = Seq(Point(0, 5), Point(0, 0), Point(5, 0), Point(5, 5))
      val clipped = Clipping.clipPolygon(polygon, Extent(2, 2))
      clipped should have length polygon.length
      clipped should contain theSameElementsAs
        Seq(Point(0, 2), Point(0, 0), Point(2, 0), Point(2, 2))
    }

    it("should return an empty Seq for a polygon completely outside the clipping region") {
      val polygon = Seq(Point(15, 250), Point(200, 300), Point(230, 150), Point(50, 220))
      Clipping.clipPolygon(polygon, Extent(10, 10)) shouldBe empty
    }

    it(
      "should properly clip a polygon when all of its points are outside the clipping region" +
        " but some of its area lies within it.") {
      val polygon = Seq(Point(10, -10), Point(10, 30), Point(25, 10))
      val expected = Vector(
        Point(20, 10d / 3d),
        Point(17.5, 0),
        Point(10, 0),
        Point(10, 20),
        Point(17.5, 20),
        Point(20, 50d / 3d)
      )
      val clipped = Clipping.clipPolygon(polygon, Extent(20, 20))
      clipped shouldEqual expected
      clipped should have length expected.length
    }

    it("should properly clip a polygon that goes clockwise") {
      val polygon = Seq(Point(10, 30), Point(10, -10), Point(25, 10))
      val expected = Vector(
        Point(20, 10d / 3d),
        Point(17.5, 0),
        Point(10, 0),
        Point(10, 20),
        Point(17.5, 20),
        Point(20, 50d / 3d)
      ).reverse
      val clipped = Clipping.clipPolygon(polygon, Extent(20, 20))
      clipped shouldEqual expected
      clipped should have length expected.length
    }
  }

  describe("Edges") {
    it("should contain colinear points") {
      Edge(Point(0, 0), Point(8, 8)) contains Point(4, 4) shouldBe true
      Edge(Point(8, 8), Point(0, 0)) contains Point(4, 4) shouldBe true
      Edge(Point(8, 8), Point(4, 0)) contains Point(5, 2) shouldBe true

    }
    it("should contain points that are in the correct side of the edge") {
      Edge(Point(0, 0), Point(8, 8)) contains Point(8, 1) shouldBe true
      Edge(Point(8, 8), Point(0, 0)) contains Point(1, 8) shouldBe true
      Edge(Point(8, 8), Point(4, 0)) contains Point(1, 7) shouldBe true
    }
    it("should not points that are in the wrong side of the edge") {
      Edge(Point(0, 0), Point(8, 8)) contains Point(1, 8) shouldBe false
      Edge(Point(8, 8), Point(0, 0)) contains Point(8, 1) shouldBe false
      Edge(Point(8, 8), Point(4, 0)) contains Point(7, 1) shouldBe false
    }

    it("should compute the intersection point with another edge, if it exists") {
      Edge(Point(0, 2), Point(2, 2))
        .intersection(Edge(Point(1.5, 3), Point(2, 0.5))) shouldBe Some(Point(1.7, 2))
    }
  }

}
