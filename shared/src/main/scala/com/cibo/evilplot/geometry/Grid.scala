/*
 * Copyright 2017 CiBO Technologies
 */

package com.cibo.evilplot.geometry

case class Grid(numRows: Int, numCols: Int, rs: Seq[Drawable], bottomPadding: Double = 0, rightPadding: Double = 0) {
  // I don't think this is true.
//  require(rs.length == numRows * numCols, s"must supply a list of ${numRows * numCols} elements")

  def apply(row: Int)(col: Int): Drawable = rs(index(row, col))

  private def index(row: Int, col: Int): Int = row * numCols + col

  def drawable: Drawable = {
    rs.grouped(numCols).map { row =>
      row.reduce { (a: Drawable, b: Drawable) =>
        a padRight rightPadding beside b
      }
    }.reduce { (a: Drawable, b: Drawable) =>
      a padBottom bottomPadding above b
    }
  }
}
