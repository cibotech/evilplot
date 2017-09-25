package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.{Drawable, Extent, Grid, WrapDrawable}
import com.cibo.evilplot.interpreter.PlotDefinitionInterpreter
import com.cibo.evilplot.plotdefs._



/** Facets creates a 2-dimensional grid of plots extracted from a generic data object.
  * object into a subtype of PlotData.
  */

class Facets(extent: Extent, facetsDef: FacetsDef) extends WrapDrawable {
    private val interpreter = PlotDefinitionInterpreter
    private val facets = facetsDef.plotDefs.map(interpreter.eval(_, None))
    override def drawable: Drawable = new Grid(facetsDef.numRows, facetsDef.numCols,
      facets, bottomPadding = 25, rightPadding = 25)
}
