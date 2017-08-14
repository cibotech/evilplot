/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.layout

import com.cibo.evilplot.geometry.{Drawable, DrawableLater, Extent}
import org.scalajs.dom.CanvasRenderingContext2D


/**
  * A GridLayout is an m x n grid in which each cell has the same extent. Optionally borders may be added to the
  * edge cells.
 *
  * @param extent, extent of whole resulting grid
  * @param numRows number of rows in grid
  * @param numCols number of columns in grid
  * @param toBeDrawn, the items to be drawn in row major order.
  * @param bottomPadding how much to pad each cell at bottom (except bottom cells). default is 0.
  * @param rightPadding how much to pad each cell at right (except left cells). default is 0.
  */
// GridLayout will group everything into one, the grid is not (yet) queryable.
class GridLayout(val extent: Extent, numRows: Int, numCols: Int, toBeDrawn: Seq[DrawableLater],
                 bottomPadding: Double = 0.0, rightPadding: Double = 0.0) extends Layout {
  require(toBeDrawn.length == numRows * numCols, f"must supply a list of ${numRows * numCols}%d elements")

  private val cellExtent = Extent(extent.width / numCols - (numCols - 1) * rightPadding,
    extent.height / numRows - (numRows - 1) * bottomPadding)
  println(cellExtent)
  private val _drawable: Drawable = {
    (for (row <- 0 until numRows) yield {
      (for {
        col <- 0 until numCols
        _bottomPadding = if (row == numRows - 1) 0 else bottomPadding
        _rightPadding = if (col == numCols - 1) 0 else rightPadding
        thingToDraw = toBeDrawn(row * numCols + col)(cellExtent) padBottom _bottomPadding padRight _rightPadding
      } yield thingToDraw).seqDistributeH
    }).seqDistributeV
  }

//  val extent: Extent = _drawable.extent
  override def draw(canvas: CanvasRenderingContext2D): Unit = _drawable.draw(canvas)
}
