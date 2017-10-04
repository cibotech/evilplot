package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.{Drawable, Extent, Grid, WrapDrawable}
import com.cibo.evilplot.interpreter.PlotDefinitionInterpreter
import com.cibo.evilplot.plotdefs._

/** Facets creates a 2-dimensional grid of plots extracted from a generic data object.
  * object into a subtype of PlotData.
  */

class Facets(extent: Extent, facetsDef: FacetsDef) extends WrapDrawable {
  private val (numRows, numCols) = (facetsDef.numRows, facetsDef.numCols)
  private val interpreter = PlotDefinitionInterpreter
  private val bottomPadding = 30
  private val rightPadding = 30
  private val subPlotSize = Extent((extent.width - (numCols - 1) * rightPadding) / numCols,
    (extent.height - (numRows - 1) * bottomPadding) / numRows)
  private val facets = facetsDef.plotDefs.map(interpreter.eval(_, Some(subPlotSize)))
  override def drawable: Drawable = new Grid(facetsDef.numRows, facetsDef.numCols,
    facets, bottomPadding = bottomPadding, rightPadding = rightPadding)
}
