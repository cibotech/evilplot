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

package com.cibo.evilplot.numeric

import org.scalactic.{Equivalence, TypeCheckedTripleEquals}
import org.scalatest.prop.PropertyChecks
import org.scalatest.{FunSpec, Matchers}

trait NumericTestSupport {
  private val tolerance = math.ulp(1.0)
  implicit object PointEquivalence extends Equivalence[Point] {
    def areEquivalent(a: Point, b: Point): Boolean =
      math.abs(a.x - b.x) < tolerance && math.abs(a.y - b.y) < tolerance
  }
}

class MarchingSquaresSpec extends FunSpec with Matchers
  with TypeCheckedTripleEquals
  with NumericTestSupport
  with PropertyChecks {

  import org.scalacheck.Gen

  val densityGrids: Gen[Vector[Vector[Double]]] = {
    val numRows = Gen.choose(10, 30)
    val numCols = 15
    val rowGen = Gen.containerOfN[Vector, Double](numCols, Gen.choose(0, 1e10)).filter(_.length == numCols)
    numRows.flatMap(m => Gen.containerOfN[Vector, Vector[Double]](m, rowGen).filter(_.length == m))
  }

  // Turn the generated grid into a GridData for testing.
  def mkGridData(g: Vector[Vector[Double]]): GridData = {
    GridData(g, Bounds(0, 10), Bounds(0, 10),
      zBounds = Bounds(g.flatten.min, g.flatten.max), 1.0, 1.0)
  }

  // Contour a grid.
  def mkContours(g: GridData): Vector[Vector[Vector[Point]]] = {
    val numContours = 6
    val levels = Seq.tabulate(numContours - 1)(bin => g.zBounds.min + (bin + 1) * (g.zBounds.range / numContours))
    MarchingSquares(levels, g)
  }

  // Inefficient way to find segments in path that intersect others.
  def intersectingSegments(points: Vector[Point]): Vector[(Vector[Point], Vector[Vector[Point]])] = {
    val sliding = points.sliding(2).toVector
    sliding.zipWithIndex.flatMap { case (segment, i) =>
      val intersecting = (sliding.take(i - 1) ++ sliding.drop(i + 1)).filter(s2 => intersect(segment, s2))
      if (intersecting.nonEmpty) Some(segment -> intersecting) else None
    }
  }

  // Test if two points intersect.
  // For the purposes of this test segments sharing an endpoint do not intersect.
  def intersect(seg1: Vector[Point], seg2: Vector[Point]): Boolean = {
    val Vector(a1, a2) = seg1
    val Vector(b1, b2) = seg2
    val boundingBoxesOverlap = a1.x <= b2.x && a2.x >= b1.x && a1.y <= b2.y && a2.y >= b1.y
    val endPointsNotEqual = (a1 !== b1) && (a1 !== b2) && (a2 !== b1) && (a2 !== b2)
    boundingBoxesOverlap && endPointsNotEqual
  }

  val tol = 1e-7

  describe("Marching Squares") {
    it("should return no contours for NaN grids") {
      val nangrid = mkGridData(Vector.fill(10)(Vector.fill(15)(Double.NaN)))
      val contours = mkContours(nangrid)
      contours.foreach { level => level shouldBe empty }
    }


    it("calculates the factor `alpha` properly") {
      val target = 0.5
      val p = 0.725
      val q = 0.125

      def alpha(p: Double, q: Double): Double = MarchingSquares.mkCalcAlpha(target)(p, q)
      alpha(p, q) shouldBe  0.375
      alpha(.2, .2) shouldBe 0
    }
  }

  describe("Properties of marching squares generated contours.") {
    it("should produce isocontours whose vertices lie on grid edges") {
      forAll((densityGrids, "grid")) { (grid: Vector[Vector[Double]]) =>
        lazy val gridData = mkGridData(grid)
        lazy val xCoordsOnEdge = Seq.tabulate(grid.length)(gridData.xBounds.min + gridData.xSpacing * _)
        lazy val yCoordsOnEdge = Seq.tabulate(grid.head.length)(gridData.yBounds.min + gridData.ySpacing * _)
        lazy val contours = mkContours(gridData)
        def onGridEdge(p: Point): Boolean =
          xCoordsOnEdge.count((x: Double) => math.abs(x - p.x) <= tol) == 1 ||
            yCoordsOnEdge.count((y: Double) => math.abs(y - p.y) <= tol) == 1
        whenever(grid.nonEmpty) {
          contours.flatten.flatten.foreach(p => onGridEdge(p) shouldBe true)
        }
      }
    }

    it("should produce isocontours that don't intersect themselves") {
      forAll((densityGrids, "grid")) { (grid: Vector[Vector[Double]]) =>
        whenever(grid.nonEmpty) {
          mkContours(mkGridData(grid)).foreach { level =>
            level.foreach { path =>
              if (path.nonEmpty) intersectingSegments(path) shouldBe empty
            }
          }
        }
      }
    }
  }
}
