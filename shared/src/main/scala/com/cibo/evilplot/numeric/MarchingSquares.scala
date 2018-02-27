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
//    println(blockPoints.flatten)
    mkPaths(blockPoints.flatten.grouped(2)).map(_.map(transform))
  }

  def isOpen(contour: Vector[Point]): Boolean = contour.head != contour.last

  //scalastyle:off
  // Takes the raw "segments" returned from the blocking step and creates
  // paths out of it. This is technically not part of Marching Squares proper,
  // but is an important consideration for drawing isocontours.
  // This implementation mostly comes from the SciKit Image implementation in Python.
  private[numeric] def mkPaths(grouped: Iterator[Seq[Point]]): Vector[Vector[Point]] = {
    @tailrec
    def mkPaths(startPoints: Map[Point, (Vector[Point], Int)],
                endPoints: Map[Point, (Vector[Point], Int)],
                contours: Map[Int, Vector[Point]],
                index: Int): Vector[Vector[Point]] = {
//      assert(contours.filter(p => isOpen(p._2)).forall(p => startPoints.contains(p._2.head) && endPoints.contains(p._2.last)
//      ), "Contours invariant not satisfied.")
//      assert(contours.count(p => isOpen(p._2)) == startPoints.size && startPoints.size == endPoints.size,
//        s"Open contours, start points, and end points must all be the same size but there were ${contours.count(p => isOpen(p._2))} open contours, " +
//      s"${startPoints.size} startpoints and ${endPoints.size} end points")
      lazy val Seq(from, to) = grouped.next()
      if (grouped.isEmpty) contours.values.toVector
      else if (from == to) {
        // Ignore
        println("case 0")
        mkPaths(startPoints, endPoints, contours, index)
      } else {
        val tails = startPoints.get(to)
        val heads = endPoints.get(from)
//        println(tails, heads)
        if (tails.isDefined && heads.isDefined) {
          val (head, headIndex) = heads.get
          val (tail, tailIndex) = tails.get
          if (head == tail) { /* close the contour */
            println("case 1a")
            mkPaths(startPoints - to,
              endPoints - from,
              contours.updated(headIndex, contours(headIndex) :+ to),
              index)
          } else if (tailIndex > headIndex) { /* "tail" comes before head in the contour */
            println("case 1b")
//            val concat = tail ++ head
            val concat = head ++ tail
            mkPaths(
              startPoints - to,
              (endPoints - (head.last, from)).updated(concat.last, concat -> headIndex),
              contours.updated(headIndex, concat) - tailIndex,
              index
            )
          } else { /* "head" comes before tail in the contour */
            println("case 1c")
            val concat = head.reverse ++ tail
            mkPaths(
              (startPoints - (head.head, to)).updated(concat.head, concat -> tailIndex),
              endPoints - from,
              contours.updated(tailIndex, concat) - headIndex,
              index
            )
          }
        } else if (tails.isDefined && heads.isEmpty) { /* Add to the beginning of endpoints. */
//          println("case 2")
          val (tail, tailIndex) = tails.get
          require(startPoints.contains(to), s"$to was not in $startPoints")
//          require(!startPoints.contains(from), s"case 2: $from was in $startPoints")
          val concat = from +: tail
          mkPaths(
            (startPoints - to).updated(from, concat -> tailIndex),
            endPoints,
            contours.updated(tailIndex, concat),
            index
          )
        } else if (tails.isEmpty && heads.isDefined) { /* Symmetric to previous case. */
//          println("case 3")
          val (head, headIndex) = heads.get
          require(endPoints.contains(from), s"$from was not in $endPoints")
//          require(!endPoints.contains(to), s"case 3: $to was in $endPoints")
          val concat = head :+ to
          mkPaths(
            startPoints,
            (endPoints - from).updated(to, concat -> headIndex),
            contours.updated(headIndex, concat),
            index
          )
        } else { /* Append a new contour. */
//          println("case 4")
          val nextContourIndex = index + 1
          val contour = Vector(from, to)
          println(contour)

//          val end = endPoints.updated(to, contour -> nextContourIndex)
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
      val ulIndexPart = if (upLeft.value > target) 1 else 0 // i.e. 1 << 3
      val urIndexPart = if (upRight.value > target) 2 else 0
      val blIndexPart = if (bottomLeft.value > target) 4 else 0
      val brIndexPart = if (bottomRight.value > target) 8 else 0
      val description = ulIndexPart | urIndexPart | brIndexPart | blIndexPart

      if (description == 6 || description == 9 && averageValue < target)
        description ^ 15 // flip it
      else description
    }
  }

  // Create a function that calculates offset from the grid index.
  private[numeric] def mkCalcAlpha(target: Double)(p: Double, q: Double): Double =
    if (p == q) 0 else (target - p) / (q - p)

  private[numeric] def indicesToCartesian(gridData: GridData)(p: Point): Point =
    Point(gridData.xSpacing * p.x + gridData.xBounds.min,
      gridData.ySpacing * p.y + gridData.yBounds.min)

  //scalastyle:off
  private[numeric] def pointsForBlock(target: Double,
                                      gb: GridBlock): Seq[Point] = {
    import gb._
    val alpha = mkCalcAlpha(target) _
    lazy val top =
      Point(upLeft.row.toDouble, upLeft.col.toDouble + alpha(upLeft.value, upRight.value))
    lazy val bottom = Point(
      bottomLeft.row.toDouble,
      bottomLeft.col.toDouble + alpha(bottomLeft.value, bottomRight.value))
    lazy val left = Point(upLeft.row.toDouble + alpha(upLeft.value, bottomLeft.value),
      upLeft.col.toDouble)
    lazy val right = Point(upRight.row.toDouble + alpha(upRight.value, bottomRight.value),
      upRight.col.toDouble)
    println(gb.tag(target))
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
        throw new IllegalStateException(
          s"Marching Squares: Block tag $x was not in [0, 16).")
    }
  }
  //scalastyle:on

}
