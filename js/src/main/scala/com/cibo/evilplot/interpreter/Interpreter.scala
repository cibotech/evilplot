/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.interpreter
import com.cibo.evilplot.colors.Colors.SingletonColorBar
import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent}
import com.cibo.evilplot.plot._
import com.cibo.evilplot.plotdefs._
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._

object PlotDefinitionInterpreter {
  def apply(definition: String): Either[Error, Drawable] = {
    decode[PlotDef](definition).right.map { right: PlotDef => eval(right) }
  }

  // smartest if this takes an actual PlotDef, this way, each PlotDef from within facets can be processed.
  private def eval(plotDef: PlotDef): Drawable = {
    plotDef match {
      case FacetsDef(size, numRows, numCols, defs, colLabels, rowLabels, axisScales, opts) =>
        val plotData: Seq[PlotData] = defs.map(getPlotData)
//        val columns: Seq[(Seq[PlotData] => Seq[PlotData])] = for (i <- 0 until numRows) yield ((x: Seq[PlotData]) => )
//        new Facets(size, )
        EmptyDrawable()
      case pd => getPlotData(pd).createPlot(pd.extent, pd.options)
    }
  }

  def getPlotData(plotDef: PlotDef): PlotData = plotDef match {
    case _: FacetsDef =>
      throw new UnsupportedOperationException ("currently arbitrary nesting of FacetsDef is not supported.")
    case ScatterPlotDef(_, data, color, _) => ScatterPlotData(data, None, colorBar = SingletonColorBar(color))
    case ContourPlotDef(_, data, nContours, _) => ContourData(data, nContours)
    case HistogramPlotDef(_, data, nBins, _) => HistogramData(data, nBins)
    case BarChartDef(_, data, labels, width, spacing, _) => BarChartData(data, labels, width, spacing)
    case BoxPlotDef(_, labels, dists, drawPoints, width, spacing, rectColor, pointColor, pointSize, _) =>
      BoxPlotData(labels, dists, drawPoints, width, spacing, rectColor, pointColor, pointSize)
  }
}
