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

import scala.annotation.tailrec

object MarchingSquares {

  /**
    * Contour a grid.
    *
    * @param levels   isocontour values
    * @param gridData grid representing the surface to be contoured.
    * @return 3D Vector of 3D points. The outer vector is for levels,
    *         second is for paths within levels, and inner is for points within paths.
    *         The paths are ordered such that they may be drawn without further manipulation.
    */
  def apply(levels: Seq[Double], gridData: GridData): Vector[Vector[Vector[Point]]] = {
    import gridData.grid
    require(
      grid.length >= 2 && grid.head.length >= 2,
      "A grid of at least dimensions 2 x 2 is required to perform contouring.")
    val blocks = for {
      cellRow <- grid.indices.init
      cellCol <- grid.head.indices.init
    } yield GridBlock(grid, cellRow, cellCol)

    levels.map(z => contourLevel(z, blocks, indicesToCartesian(gridData))).toVector
  }

  // Contour at level, given a function for transforming index space to Cartesian.
  private[numeric] def contourLevel(
    level: Double,
    blocks: Seq[GridBlock],
    transform: Point => Point): Vector[Vector[Point]] = {
    val blockPoints = blocks.map(b => pointsForBlock(level, b))
    mkPaths(blockPoints.flatten.grouped(2)).map(_.map(transform))
  }

  //scalastyle:off
  // Takes the raw "segments" returned from the blocking step and creates
  // paths out of it. This is technically not part of Marching Squares proper,
  // but is an important consideration for drawing isocontours.
  // This implementation mostly comes from the SciKit Image implementation in Python.
  private[numeric] def mkPaths(grouped: Iterator[Seq[Point]]): Vector[Vector[Point]] = {
    // Maintain lists of starting points of contours, endpoints, and current contours
    // at this level we know about.
    @tailrec
    def mkPaths(
      startPoints: Map[Point, (Vector[Point], Int)],
      endPoints: Map[Point, (Vector[Point], Int)],
      contours: Map[Int, Vector[Point]],
      index: Int): Vector[Vector[Point]] = {
      lazy val Seq(from, to) = grouped.next()
      if (grouped.isEmpty) contours.values.toVector
      else if (from == to) {
        // Ignore it.
        mkPaths(startPoints, endPoints, contours, index)
      } else {
        val tails = startPoints.get(to)
        val heads = endPoints.get(from)
        if (tails.isDefined && heads.isDefined) {
          val (head, headIndex) = heads.get
          val (tail, tailIndex) = tails.get
          if (head == tail) {
            /* close the contour */
            mkPaths(
              startPoints - to,
              endPoints - from,
              contours.updated(headIndex, head :+ to),
              index)
          } else if (tailIndex > headIndex) {
            /* "tail" comes before head in the contour */
            val concat = head ++ tail
            mkPaths(
              startPoints - to,
              (endPoints - (tail.last, from)).updated(concat.last, concat -> headIndex),
              (contours - tailIndex).updated(headIndex, concat),
              index
            )
          } else {
            /* "head" comes before tail in the contour */
            val concat = head ++ tail
            mkPaths(
              (startPoints - (head.head, to)).updated(concat.head, concat -> tailIndex),
              endPoints - from,
              (contours - headIndex).updated(tailIndex, concat),
              index
            )
          }
        } else if (tails.isDefined && heads.isEmpty) {
          /* Add to the beginning of endpoints. */
          val (tail, tailIndex) = tails.get
          val concat = from +: tail
          mkPaths(
            (startPoints - to).updated(from, concat -> tailIndex),
            endPoints,
            contours.updated(tailIndex, concat),
            index
          )
        } else if (tails.isEmpty && heads.isDefined) {
          /* Symmetric to previous case. */
          val (head, headIndex) = heads.get
          val concat = head :+ to
          mkPaths(
            startPoints,
            (endPoints - from).updated(to, concat -> headIndex),
            contours.updated(headIndex, concat),
            index
          )
        } else {
          /* Append a new contour. */
          val nextContourIndex = index + 1
          val contour = Vector(from, to)

          mkPaths(
            startPoints.updated(from, contour -> nextContourIndex),
            endPoints.updated(to, contour -> nextContourIndex),
            contours + (nextContourIndex -> contour),
            nextContourIndex
          )
        }
      }
    }
    mkPaths(Map.empty, Map.empty, Map.empty, 0)
  }
  //scalastyle:on

  private[numeric] case class GridCell(row: Int, col: Int, value: Double)

  // Represents a 2 x 2 block of cells in the original grid.
  private[numeric] case class GridBlock(grid: Grid, cellRow: Int, cellCol: Int) {
    require(
      cellRow + 1 < grid.length && cellCol + 1 < grid.head.length,
      "not enough room to make GridBlock here")
    val upLeft = GridCell(cellRow, cellCol, grid(cellRow)(cellCol))
    val upRight = GridCell(cellRow, cellCol + 1, grid(cellRow)(cellCol + 1))
    val bottomRight = GridCell(cellRow + 1, cellCol + 1, grid(cellRow + 1)(cellCol + 1))
    val bottomLeft = GridCell(cellRow + 1, cellCol, grid(cellRow + 1)(cellCol))
    lazy val averageValue = (upLeft.value + upRight.value + bottomRight.value + bottomLeft.value) / 4.0

    /* A 4 bit tag, representing this block, from msb to lsb in left-right-top-bottom order.
     * Each bit represents whether the array value is > or <= than the target we're trying to contour.
     * Tags 6 and 9 are considered ambiguous; we adopt the heuristic that if that average block value is <= target
     * and the tag is 6 or 9, it gets switched to the other. */
    private[numeric] def tag(target: Double): Int = {
      val ulIndexPart = if (upLeft.value > target) 1 else 0 // i.e. 1 << 3
      val urIndexPart = if (upRight.value > target) 2 else 0
      val blIndexPart = if (bottomLeft.value > target) 4 else 0
      val brIndexPart = if (bottomRight.value > target) 8 else 0
      val description = ulIndexPart | urIndexPart | brIndexPart | blIndexPart

      if (description == 6 || description == 9 && averageValue <= target)
        description ^ 15 // flip it
      else description
    }
  }

  // Create a function that calculates offset from the grid index.
  private[numeric] def mkCalcAlpha(target: Double)(p: Double, q: Double): Double =
    if (p == q) 0 else (target - p) / (q - p)

  private[numeric] def indicesToCartesian(gridData: GridData)(p: Point): Point =
    Point(
      gridData.xSpacing * p.x + gridData.xBounds.min,
      gridData.ySpacing * p.y + gridData.yBounds.min)

  //scalastyle:off
  private[numeric] def pointsForBlock(target: Double, gb: GridBlock): Seq[Point] = {
    import gb._
    val alpha = mkCalcAlpha(target) _
    lazy val top =
      Point(upLeft.row.toDouble, upLeft.col.toDouble + alpha(upLeft.value, upRight.value))
    lazy val bottom = Point(
      bottomLeft.row.toDouble,
      bottomLeft.col.toDouble + alpha(bottomLeft.value, bottomRight.value))
    lazy val left =
      Point(upLeft.row.toDouble + alpha(upLeft.value, bottomLeft.value), upLeft.col.toDouble)
    lazy val right =
      Point(upRight.row.toDouble + alpha(upRight.value, bottomRight.value), upRight.col.toDouble)

    gb.tag(target) match {
      case 0 | 15 => Vector.empty[Point]
      case 1      => Vector(top, left)
      case 2      => Vector(right, top)
      case 3      => Vector(right, left)
      case 4      => Vector(left, bottom)
      case 5      => Vector(top, bottom)
      case 6      => Vector(left, top, right, bottom)
      case 7      => Vector(right, bottom)
      case 8      => Vector(bottom, right)
      case 9      => Vector(top, right, bottom, left)
      case 10     => Vector(bottom, top)
      case 11     => Vector(bottom, left)
      case 12     => Vector(left, right)
      case 13     => Vector(top, right)
      case 14     => Vector(left, top)
      case x =>
        throw new IllegalStateException(s"Marching Squares: Block tag $x was not in [0, 16).")
    }
  }
  //scalastyle:on

}
