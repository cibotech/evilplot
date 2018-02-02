package com.cibo.evilplot.oldplot

import com.cibo.evilplot.geometry._
import com.cibo.evilplot.interpreter.PlotDefinitionInterpreter
import com.cibo.evilplot.plotdefs._

/** Facets creates a 2-dimensional grid of plots extracted from a generic data object.
  * object into a subtype of PlotData.
  */

object Facets {
  type AxisType = (FacetsDef => Option[String], Double)
  val xAxis: AxisType = (fd => fd.options.xAxisLabel, 0)
  val yAxis: AxisType = (fd => fd.options.yAxisLabel, 270)

  def axisLabel(fd: FacetsDef, axis: AxisType): Drawable = axis._1(fd) match {
    case Some(label) => Text(label, 20) padAll 10 rotated axis._2
    case None => EmptyDrawable()
  }
}

case class Facets(extent: Extent, facetsDef: FacetsDef) {
  import Facets._
  private val (numRows, numCols) = (facetsDef.numRows, facetsDef.numCols)
  private val interpreter = PlotDefinitionInterpreter
  private val bottomPadding = 30
  private val rightPadding = 30
  private val xLabel: Drawable = axisLabel(facetsDef, xAxis)
  private val yLabel: Drawable = axisLabel(facetsDef, yAxis)

  private val subPlotSize = Extent(
    (extent.width - yLabel.extent.width - (numCols - 1) * rightPadding) / numCols,
    (extent.height - xLabel.extent.height - (numRows - 1) * bottomPadding) / numRows)

  private val facets = facetsDef.plotDefs.map(interpreter.eval(_, Some(subPlotSize)))

  def drawable: Drawable = {
    Align.center(
      Align.middle(
        yLabel,
        Grid(facetsDef.numRows, facetsDef.numCols, facets, bottomPadding = bottomPadding, rightPadding = rightPadding).drawable
      ).reduce(beside),
      xLabel transX yLabel.extent.width
    ).reduce(above)
  }
}
