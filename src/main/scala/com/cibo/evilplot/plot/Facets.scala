package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.{Drawable, DrawableLaterMaker, Extent, WrapDrawable}
import com.cibo.evilplot.layout.GridLayout
import com.cibo.evilplot.plot.Facets._

/** Facets creates a 2-dimensional grid of plots extracted from a generic data object.
  * The caller supplies sequences of "row" accessors and "col" accessors that when composed transform the generic data
  * object into a subtype of PlotData.
  */
// TODO: could rename row and column accessor seqs to first and second, and give an option for the dimension along
// which to lay things out
object Facets {
  sealed trait ScaleOption
  case object FixedScales extends ScaleOption
  case object FreeScales extends ScaleOption
  // TODO: implement these
/*  case object FixedX extends ScaleOption
  case object FixedY extends ScaleOption*/
}

class Facets[T, U, V <: PlotData](extent: Extent, dataObject: T, columns: Seq[(T => U)],
                                  rows: Seq[(U => V)], columnLabels: Option[Seq[String]] = None,
                                  rowLabels: Option[Seq[String]] = None, axisScales: ScaleOption = FixedScales,
                                  baseOptions: PlotOptions = PlotOptions()) extends WrapDrawable {
  private val numCols = columns.length
  private val numRows = rows.length

  // TODO: these may need to be refactored. there is additional complexity in fixing scales for a histogram.
  private def getExtrema(allBounds: Seq[Bounds]) = {
    if (allBounds.nonEmpty) Some(Bounds(allBounds.minBy(_.min).min, allBounds.maxBy(_.max).max)) else None
  }

  private def extremaAndTicks(subset: Seq[V]): (Option[Bounds], Option[Bounds]) = {
    val xExtrema: Option[Bounds] = getExtrema(subset.flatMap(_.xBounds))
    val yExtrema: Option[Bounds] = xExtrema match {
      case Some(xBounds) => getExtrema((for (plotData <- subset) yield {
        plotData match {
          case histData: HistogramData => Some(histData.histogramBounds(xBounds))
          case otherData: PlotData => otherData.yBounds
        }
      }).flatten)
      case None => getExtrema(subset.flatMap(_.yBounds))
    }
    (xExtrema, yExtrema)
  }

  private def recalculateFacet(facet: PlotData, xBounds: Option[Bounds]): PlotData = {
    facet match {
      case f: HistogramData => f.copy(bounds = xBounds)
      case _ => facet
    }
  }

  private def buildPlotDataAndOptions(allFacetData: Seq[V]): Seq[(PlotData, PlotOptions)] = {
    lazy val (xExtrema, yExtrema) = extremaAndTicks(allFacetData)

    def inTopRow(row: Int): Boolean = row == 0; def inBottomRow(row: Int): Boolean = row == numRows - 1
    def inLeftColumn(col: Int): Boolean = col == 0; def inRightColumn(col: Int): Boolean = col == numCols - 1

    for {(_facet, index) <- allFacetData.zipWithIndex
         row = index / numCols
         col = index % numCols
         (facet, xAxisBounds, yAxisBounds, drawXAxis, drawYAxis) = axisScales match {
           case FixedScales =>
             (recalculateFacet(_facet, xExtrema), xExtrema, yExtrema, inBottomRow(row), inLeftColumn(col))
           case FreeScales => (_facet, None, None, true, true)
         }
         rightLabel = rowLabels match { case Some(labels) if inRightColumn(col) => Some(labels(row)); case _ => None }
         topLabel = columnLabels match { case Some(labels) if inTopRow(row) => Some(labels(col)); case _ => None }

    } yield (facet, baseOptions.copy(xAxisBounds = xAxisBounds, yAxisBounds = yAxisBounds, drawXAxis = drawXAxis,
      drawYAxis = drawYAxis, topLabel = topLabel, rightLabel = rightLabel))
  }
  val allFacetData: Seq[V] = for {
    row <- rows
    col <- columns
  } yield row(col(dataObject))


  private val _drawable = {
    def createChart(facetData: PlotData, options: PlotOptions)(_extent: Extent): Drawable =
      facetData.createPlot(_extent, options)
    val facets = for ((facetData, options) <- buildPlotDataAndOptions(allFacetData))
      yield new DrawableLaterMaker(createChart(facetData, options))
    new GridLayout(extent, numRows, numCols, facets)
  }
  override def drawable: Drawable = _drawable
}
