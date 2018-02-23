/*
 * Copyright 2017 CiBO Technologies
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
  def apply(levels: Seq[Double],
            gridData: GridData): Vector[Vector[Vector[Point]]] = {
    import gridData.grid
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

  // Takes the raw "segments" returned from the blocking step and creates
  // paths out of it. This is technically not part of Marching Squares proper,
  // but is an important consideration for drawing isocontours.
  // This mostly comes from this Python implementation:
  private[numeric] def mkPaths(grouped: Iterator[Seq[Point]]): Vector[Vector[Point]] = {
    @tailrec
    def mkPaths(startPoints: Map[Point, (Vector[Point], Int)],
                endPoints: Map[Point, (Vector[Point], Int)],
                contours: Map[Int, Vector[Point]],
                index: Int): Vector[Vector[Point]] = {
      lazy val Seq(from, to) = grouped.next()
      if (grouped.isEmpty) contours.values.toVector
      else if (from == to) {
        // Ignore
        print("case 0")
        mkPaths(startPoints, endPoints, contours, index)
      }else {
        val start = startPoints.get(from)
        val end = endPoints.get(to)
        if (start.isDefined && end.isDefined) {
          println("case 1")
          val (head, headIndex) = start.get
          val (tail, tailIndex) = end.get
          if (head == tail) { /* close the contour */
            mkPaths(startPoints - to,
              endPoints - from,
              contours.updated(headIndex, contours(headIndex) :+ to),
              index)
          } else if (tailIndex > headIndex) { /* "tail" comes before head in the contour */
            val concat = tail ++ head
            mkPaths(
              startPoints - to,
              (endPoints - (head.last, from)).updated(concat.last, concat -> headIndex),
              contours.updated(headIndex, concat) - tailIndex,
              index
            )
          } else { /* "head" comes before tail in the contour */
            val concat = head.reverse ++ tail
            val split = contours.updated(tailIndex, concat).splitAt(headIndex)
            mkPaths(
              (startPoints - (head.head, to)).updated(concat.head, concat -> tailIndex),
              endPoints - from,
              contours.updated(tailIndex, concat) - headIndex,
              index
            )
          }
        } else if (start.isEmpty && end.nonEmpty) { /* Add to the beginning of endpoints. */
          val (tail, tailIndex) = end.get
          val concat = from +: tail
          mkPaths(
            startPoints.updated(from, concat -> tailIndex) - to,
            endPoints,
            contours.updated(tailIndex, concat),
            index
          )
        } else if (start.isDefined && end.isEmpty) { /* Symmetric to previous case. */
          val (head, headIndex) = start.get
          val concat = head :+ to
          mkPaths(
            startPoints,
            endPoints.updated(to, concat -> headIndex) - from,
            contours.updated(headIndex, concat),
            index
          )
        } else { /* Append a new contour. */
          val nextContourIndex = index + 1
          val contour = Vector(from, to)
          mkPaths(
            startPoints + (from -> (contour -> nextContourIndex)),
            endPoints + (to -> (contour -> nextContourIndex)),
            contours + (nextContourIndex -> contour),
            nextContourIndex
          )
        }
      }
    }
    mkPaths(Map.empty, Map.empty, Map.empty, 0)
  }

  private[numeric] case class GridCell(row: Int, col: Int, value: Double)

  // Represents a 2 x 2 block of cells in the original grid.
  private[numeric] case class GridBlock(grid: Grid,
                                        cellRow: Int,
                                        cellCol: Int) {
    require(cellRow + 1 < grid.length && cellCol + 1 < grid.head.length,
      "not enough room to make GridBlock here")
    val upLeft = GridCell(cellRow, cellCol, grid(cellRow)(cellCol))
    val upRight = GridCell(cellRow, cellCol + 1, grid(cellRow)(cellCol + 1))
    val bottomRight = GridCell(cellRow + 1, cellCol + 1, grid(cellRow + 1)(cellCol + 1))
    val bottomLeft = GridCell(cellRow + 1, cellCol, grid(cellRow + 1)(cellCol))
    lazy val averageValue = (upLeft.value + upRight.value + bottomRight.value + bottomLeft.value) / 4.0

    /* A 4 bit tag, representing this block, from msb to lsb clockwise starting at the upper left cell.
     * Each bit represents whether a cell is "positive" or "negative" (if its z value is >= or < than the target z
     * we're trying to contour).
     * tags 5 and 10 are considered ambiguous; we adopt the heuristic that if that average block value is < target
     * and the tag is 5 or 10, it gets switched to the other. */
    private[numeric] def tag(target: Double): Int = {
      val ulIndexPart = if (upLeft.value >= target) 8 else 0 // i.e. 1 << 3
      val urIndexPart = if (upRight.value >= target) 4 else 0
      val brIndexPart = if (bottomRight.value >= target) 2 else 0
      val blIndexPart = if (bottomLeft.value >= target) 1 else 0
      val description = ulIndexPart | urIndexPart | brIndexPart | blIndexPart

      if (description == 0x5 || description == 0xa && averageValue < target)
        description ^ 0xf // flip it
      else description
    }
  }

  // Create a function that calculates offset from the grid index.
  private[numeric] def mkCalcAlpha(target: Double)(p: Double, q: Double): Double =
    (target - p) / (q - p)

  private[numeric] def indicesToCartesian(gridData: GridData)(p: Point): Point =
    Point(gridData.xSpacing * p.x + gridData.xBounds.min,
      gridData.ySpacing * p.y + gridData.yBounds.min)

  //scalastyle:off
  private[numeric] def pointsForBlock(target: Double,
                                      gb: GridBlock): Seq[Point] = {
    import gb._
    val alpha = mkCalcAlpha(target) _
    lazy val top =
      Point(upLeft.row.toDouble, upRight.col.toDouble + alpha(upLeft.value, upRight.value))
    lazy val bottom = Point(
      bottomLeft.row.toDouble,
      bottomRight.col.toDouble + alpha(bottomLeft.value, bottomRight.value))
    lazy val left = Point(upLeft.row.toDouble + alpha(upLeft.value, bottomLeft.value),
      upLeft.col.toDouble)
    lazy val right = Point(upRight.row.toDouble + alpha(upRight.value, bottomRight.value),
      upRight.col.toDouble)

    gb.tag(target) match {
      case 0 | 15 => Vector.empty[Point]
      case 1      => Vector(bottom, left)
      case 14     => Vector(left, bottom)
      case 2      => Vector(right, bottom)
      case 13     => Vector(bottom, right)
      case 3      => Vector(left, right)
      case 12     => Vector(right, left)
      case 4      => Vector(left, top)
      case 11     => Vector(top, left)
      case 5      => Vector(left, top, right, bottom)
      case 10     => Vector(top, right, bottom, left)
      case 6      => Vector(top, bottom)
      case 9      => Vector(bottom, top)
      case 7      => Vector(right, top)
      case 8      => Vector(top, right)
      case x =>
        throw new IllegalStateException(
          s"Marching Squares: Block tag $x was not in [0, 16).")
    }
  }
  //scalastyle:on

}
