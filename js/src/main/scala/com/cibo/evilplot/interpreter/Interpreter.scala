/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.interpreter
import com.cibo.evilplot.JSONUtils
import com.cibo.evilplot.geometry.{Drawable, Extent}
import com.cibo.evilplot.plot._
import com.cibo.evilplot.plotdefs._
import io.circe.generic.auto._

/** If an extent is supplied to the PlotDefinitionInterpreter, the serialized plot extent is overridden. */
object PlotDefinitionInterpreter {
  val defaultSize = Extent(800, 400) // completely arbitrary, can change later.
  def apply(definition: String, extent: Option[Extent] = None): Drawable = {
    val plotDef = JSONUtils.decodeStr[PlotDef](definition)
    eval(plotDef, extent)
  }

  def eval(plotDef: PlotDef, extent: Option[Extent]): Drawable = {
    def getSize(pd: PlotDef): Extent = extent.getOrElse(pd.extent.getOrElse(defaultSize))
    plotDef match {
      case scatter: ScatterPlotDef =>
        new ScatterPlot(getSize(scatter), scatter)
      case contour: ContourPlotDef =>
        new ContourPlot(getSize(contour), contour)
      case histogram: HistogramChartDef =>
        new HistogramChart(getSize(histogram), histogram)
      case barChart: BarChartDef =>
        new BarChart(getSize(barChart), barChart)
      case boxPlot: BoxPlotDef =>
        new BoxPlotChart(getSize(boxPlot), boxPlot)
      case linePlot: LinePlotDef =>
        new LinePlot(getSize(linePlot), linePlot)
      case facetsDef: FacetsDef => new Facets(getSize(facetsDef), facetsDef)
    }
  }

}
