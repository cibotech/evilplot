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

import com.cibo.evilplot.numeric.{Point, Point2d}

object Clipping {

  protected[evilplot] final case class Edge(p1: Point2d, p2: Point2d) {
    lazy val vertical: Boolean = p1.x == p2.x
    lazy val slope: Double = (p2.y - p1.y) / (p2.x - p1.x)
    lazy val intercept: Double = -slope * p1.x + p1.y

    def valueAt(x: Double): Double = slope * x + intercept

    def intersection(edge: Edge): Option[Point2d] = {
      if (vertical && edge.vertical) {
        None
      } else if (edge.vertical) {
        Some(Point(edge.p1.x, slope * edge.p1.x + intercept))
      } else if (vertical) {
        Some(Point(p1.x, edge.slope * p1.x + edge.intercept))
      } else {
        val interceptX = (edge.intercept - intercept) / (slope - edge.slope)
        val interceptY = valueAt(interceptX)
        Some(Point(interceptX, interceptY))
      }
    }
    /*
      This calculates whether the point is on the inside of our edge or not. this crossproduct gives us double the area
      of the triangle, with a positive sign if it's on the outside, and a negative sign if it's on the inside. 0 means it's
      colinear.
     */
    def contains(query: Point2d): Boolean = {
      crossProduct(p1, p2, query) <= 0
    }
  }

  // (p2 - p1) cross (p3 - p1)
  private[evilplot] def crossProduct(p1: Point2d, p2: Point2d, p3: Point2d): Double = {
    (p2.x - p1.x) * (p3.y - p1.y) - (p3.x - p1.x) * (p2.y - p1.y)
  }

  private def boundEdges(extent: Extent): Seq[Edge] = {
    Seq(
      Edge(Point(extent.width, 0), Point(0, 0)),
      Edge(Point(0, 0), Point(0, extent.height)),
      Edge(Point(0, extent.height), Point(extent.width, extent.height)),
      Edge(Point(extent.width, extent.height), Point(extent.width, 0))
    )
  }

  private def segmentPathByEdge(path: Seq[Point2d], clipEdge: Edge): Vector[Vector[Point2d]] = {
    if (path.nonEmpty) {
      val init = (
        path.head,
        Vector.empty[Vector[Point2d]],
        if (clipEdge.contains(path.head)) Vector(path.head) else Vector.empty[Point]
      )

      val (_, segmented, remainder) = path.tail.foldLeft(init) {
        case ((s, allSegments, currentSegment), point) =>
          if (clipEdge.contains(point)) {
            if (!clipEdge.contains(s)) {
              (
                point,
                allSegments,
                currentSegment ++ clipEdge.intersection(Edge(s, point)).toSeq :+ point)
            } else {
              (point, allSegments, currentSegment :+ point)
            }
          } else if (clipEdge.contains(s)) {
            (
              point,
              allSegments :+ (currentSegment ++ clipEdge.intersection(Edge(s, point)).toSeq),
              Vector.empty[Point])
          } else {
            (point, allSegments, currentSegment)
          }
      }

      if (remainder.nonEmpty) segmented :+ remainder else segmented
    } else {
      Vector.empty[Vector[Point]]
    }
  }

  def clipPath(points: Seq[Point2d], extent: Extent): Seq[Seq[Point2d]] = {
    boundEdges(extent).foldLeft(Seq(points.toVector)) { (segments, clipEdge) =>
      segments.foldLeft(Vector.empty[Vector[Point2d]]) { (acc, segment) =>
        val segments = segmentPathByEdge(segment, clipEdge)
        if (segments.nonEmpty) acc ++ segments else acc
      }
    }
  }

  // https://en.wikipedia.org/wiki/Sutherland%E2%80%93Hodgman_algorithm
  def clipPolygon(points: Seq[Point2d], extent: Extent): Seq[Point2d] = {
    boundEdges(extent).foldLeft(points.toVector) { (inputList, clipEdge) =>
      if (inputList.isEmpty) Vector.empty
      else {
        val init = (inputList.last, Vector.empty[Point2d])
        inputList
          .foldLeft(init) {
            case ((s, outputList), point) =>
              if (clipEdge.contains(point)) {
                if (!clipEdge.contains(s)) {
                  (point, outputList ++ clipEdge.intersection(Edge(s, point)).toSeq :+ point)
                } else {
                  (point, outputList :+ point)
                }
              } else if (clipEdge.contains(s)) {
                (point, outputList ++ clipEdge.intersection(Edge(s, point)).toSeq)
              } else {
                (point, outputList)
              }
          }
          ._2
      }
    }
  }
}
