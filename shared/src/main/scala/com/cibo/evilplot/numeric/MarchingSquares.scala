/*
 * Copyright 2017 CiBO Technologies
 */

package com.cibo.evilplot.numeric

// Implements Lorensen and Cline's Marching Squares algorithm.
// Some algorithm invariants are protected by assertions now to ease debugging. Move these to tests.
object MarchingSquares {
  private[numeric] case class GridCell(row: Int, col: Int, value: Double)

  private[numeric] type CellEdge = (GridCell, GridCell)

  // Represents a 2 x 2 block of cells in the original grid.
  private[numeric] class GridBlock(grid: Grid, cellRow: Int, cellCol: Int) {
    assert(cellRow + 1 < grid.length && cellCol + 1 < grid.head.length, "not enough room to make GridBlock here")
    val upLeft = GridCell(cellRow, cellCol, grid(cellRow)(cellCol))
    val upRight = GridCell(cellRow, cellCol + 1, grid(cellRow)(cellCol + 1))
    val bottomRight = GridCell(cellRow + 1, cellCol + 1, grid(cellRow + 1)(cellCol + 1))
    val bottomLeft = GridCell(cellRow + 1, cellCol, grid(cellRow + 1)(cellCol))
    lazy val top: CellEdge = (upLeft, upRight)
    lazy val left: CellEdge = (upLeft, bottomLeft)
    lazy val right: CellEdge = (upRight, bottomRight)
    lazy val bottom: CellEdge = (bottomLeft, bottomRight)
    lazy val averageValue: Double = (upLeft.value + upRight.value + bottomRight.value + bottomLeft.value) / 4.0

    /** A 4 bit tag, representing this block, from msb to lsb clockwise starting at the upper left cell.
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

      if (description == 0x5 || description == 0xa && averageValue < target) description ^ 0xf // flip it
      else description
    }
  }

  def getContoursAt(target: Double, gridData: GridData): Seq[Segment] = {
    val grid: Grid = gridData.grid

    def indicesToCartesian(indices: Point): Point = indices match {
      case Point(row, col) =>
        Point(gridData.xSpacing * row + gridData.xBounds.min, gridData.ySpacing * col + gridData.yBounds.min)
    }

    // p and q form a "bipolar" edge, i.e. one point is "positive," one is "negative"
    def interpolate(e: CellEdge): Point = {
      val (p, q) = e
      assert(p.value >= target && q.value < target || p.value < target && q.value >= target,
        "interpolate called on edge not satisfying bipolar property")
      val alpha = (target - p.value) / (q.value - p.value)

      def component(_p: Int, _q: Int): Double = (1 - alpha) * _p + alpha * _q

      Point(component(p.row, q.row), component(p.col, q.col))
    }

    // TODO: there is degeneracy here, so that should be factored out from this error prone hardcoding.
    lazy val lookupTable: Vector[(GridBlock) => Seq[Segment]] = Vector(
      (_: GridBlock) => Seq[Segment](), // 0x0
      (gb: GridBlock) => Seq(Segment(interpolate(gb.bottom), interpolate(gb.left))),
      (gb: GridBlock) => Seq(Segment(interpolate(gb.right), interpolate(gb.bottom))),
      (gb: GridBlock) => Seq(Segment(interpolate(gb.left), interpolate(gb.right))),
      (gb: GridBlock) => Seq(Segment(interpolate(gb.top), interpolate(gb.right))),
      (gb: GridBlock) => Seq(Segment(interpolate(gb.left), interpolate(gb.top)),
        Segment(interpolate(gb.bottom), interpolate(gb.right))),
      (gb: GridBlock) => Seq(Segment(interpolate(gb.top), interpolate(gb.bottom))),
      (gb: GridBlock) => Seq(Segment(interpolate(gb.left), interpolate(gb.top))),
      (gb: GridBlock) => Seq(Segment(interpolate(gb.left), interpolate(gb.top))),
      (gb: GridBlock) => Seq(Segment(interpolate(gb.top), interpolate(gb.bottom))),
      (gb: GridBlock) => Seq(Segment(interpolate(gb.left), interpolate(gb.bottom)),
        Segment(interpolate(gb.right), interpolate(gb.top))),
      (gb: GridBlock) => Seq(Segment(interpolate(gb.top), interpolate(gb.right))),
      (gb: GridBlock) => Seq(Segment(interpolate(gb.left), interpolate(gb.right))),
      (gb: GridBlock) => Seq(Segment(interpolate(gb.right), interpolate(gb.bottom))),
      (gb: GridBlock) => Seq(Segment(interpolate(gb.bottom), interpolate(gb.left))),
      (_: GridBlock) => Seq[Segment]() // 0xF
    )

    def contourSegments(block: GridBlock): Seq[Segment] = {
      lookupTable(block.tag(target))(block).map { case Segment(a, b) =>
        Segment(indicesToCartesian(a), indicesToCartesian(b))
      }
    }

    // We're blocking in 2 x 2 segments, hence the .init
    val contours = for {
      cellRow <- grid.indices.init
      cellCol <- grid.head.indices.init
      block = new GridBlock(grid, cellRow, cellCol)
    } yield contourSegments(block)

    contours.flatten
  }
}
