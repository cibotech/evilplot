package com.cibo.evilplot.numeric

import org.scalactic.{Equivalence, TypeCheckedTripleEquals}
import org.scalatest.{FunSpec, Matchers}

trait NumericTestSupport {
  private val tolerance = math.ulp(1.0)
  implicit object PointEquivalence extends Equivalence[Point] {
    def areEquivalent(a: Point, b: Point): Boolean =
      math.abs(a.x - b.x) < tolerance && math.abs(a.y - b.y) < tolerance
  }
}

class MarchingSquaresSpec extends FunSpec with Matchers with TypeCheckedTripleEquals with NumericTestSupport {
  describe("MarchingSquares") {
    val grid = Vector(
      Vector(0.795, 0.911, 0.649, 0.797, 0.967, 0.141, 0.552, 0.821, 0.997, 0.734),
      Vector(0.643, 0.856, 0.213, 0.185, 0.279, 0.563, 0.664, 0.841, 0.630, 0.558),
      Vector(0.982, 0.082, 0.320, 0.751, 0.089, 0.334, 0.516, 0.414, 0.533, 0.532),
      Vector(0.069, 0.995, 0.158, 0.107, 0.500, 0.824, 0.947, 0.130, 0.268, 0.680),
      Vector(0.036, 0.672, 0.596, 0.622, 0.696, 0.518, 0.367, 0.207, 0.219, 0.498),
      Vector(0.484, 0.352, 0.021, 0.406, 0.676, 0.976, 0.182, 0.373, 0.295, 0.133),
      Vector(0.950, 0.013, 0.091, 0.015, 0.220, 0.291, 0.024, 0.847, 0.040, 0.171),
      Vector(0.092, 0.922, 0.527, 0.684, 0.873, 0.977, 0.707, 0.942, 0.744, 0.935),
      Vector(0.630, 0.151, 0.324, 0.140, 0.053, 0.117, 0.230, 0.983, 0.250, 0.636),
      Vector(0.926, 0.493, 0.850, 0.172, 0.278, 0.952, 0.531, 0.548, 0.295, 0.150)
    )
    val zBounds = Bounds(grid.flatten.min, grid.flatten.max)
    val numRows = grid.length
    val numCols = grid.head.length
    val xBounds = Bounds(0, 10)
    val yBounds = Bounds(0, 10)
    val xSpacing = 1.0
    val ySpacing = 1.0
    val xCoordsOnEdge = Seq.tabulate(numCols)(xBounds.min + xSpacing * _)
    val yCoordsOnEdge = Seq.tabulate(numRows)(yBounds.min + ySpacing * _)

    val gd = GridData(grid, xBounds = xBounds, yBounds = yBounds, zBounds = zBounds, xSpacing, ySpacing)
    val numContours = 6
    val levels = Seq.tabulate(numContours - 1)(bin => gd.zBounds.min + (bin + 1) * (gd.zBounds.range / numContours))
    val contours = MarchingSquares(levels, gd)
    val tol = 1e-7

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

    it("should produce isocontours whose vertices lie on grid edges") {
      def onGridEdge(p: Point): Boolean =
        xCoordsOnEdge.count((x: Double) => math.abs(x - p.x) <= tol) == 1 ||
          yCoordsOnEdge.count((y: Double) => math.abs(y - p.y) <= tol) == 1

      contours.flatten.flatten.foreach(p => onGridEdge(p) shouldBe true)
    }

    it("should produce isocontours that don't intersect themselves") {
      contours.foreach { level =>
        level.foreach { path =>
          if (path.nonEmpty) intersectingSegments(path) shouldBe empty
        }
      }
    }
  }
}
