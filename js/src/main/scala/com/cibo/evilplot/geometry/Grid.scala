/*
 * Copyright 2017 CiBO Technologies
 */

package com.cibo.evilplot.geometry

class Grid(numRows: Int, numCols: Int, rs: Seq[Drawable], val bottomPadding: Double = 0, val rightPadding: Double = 0)
  extends WrapDrawable {
  require(rs.length == numRows * numCols, s"must supply a list of ${numRows * numCols} elements")

  def apply(row: Int)(col: Int): Drawable = rs(index(row, col))

  private def index(row: Int, col: Int): Int = row * numCols + col

  override def drawable: Drawable = rs.grouped(numCols).map { row =>
    row.reduce((a: Drawable, b: Drawable) => a padRight rightPadding beside b) }
      .reduce((a: Drawable, b: Drawable) => a padBottom bottomPadding above b)
}
