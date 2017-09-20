/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.interpreter
import com.cibo.evilplot.geometry.{Drawable, Extent}
import com.cibo.evilplot.numeric.BoxPlot
import com.cibo.evilplot.plot._
import com.cibo.evilplot.plotdefs._
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._

/** If an extent is supplied to the PlotDefinitionInterpreter, the serialized plot extent is overridden. */
trait PlotDefinitionInterpreter {
  protected val defaultSize: Extent
  def apply(definition: String, extent: Option[Extent]= None): Either[Error, Drawable] = {
    decode[PlotDef](definition).right.map { right: PlotDef => eval(right, extent) }
  }

  // Interesting how all of these plot classes, through no effort of my own, have
  // exactly the same signature (except Scatter).
  // Given that, it actually seems like we need to pair definition classes to constructors, given
  // the same arguments.
  private def eval(plotDef: PlotDef, extent: Option[Extent]): Drawable = {
    def getSize(pd: PlotDef): Extent = extent.getOrElse(pd.extent.getOrElse(defaultSize))
    plotDef match {
      case scatter: ScatterPlotDef =>
        new ScatterPlot(getSize(scatter), scatter.data, scatter.zData,
                        scatter.options, scatter.pointSize, scatter.colorBar)
      case contour: ContourPlotDef =>
        new ContourPlot(getSize(contour), contour, contour.options)
      case histogram: HistogramChartDef =>
        new HistogramChart(getSize(histogram), histogram, histogram.options)
      case barChart: BarChartDef =>
        new BarChart(getSize(barChart), barChart, barChart.options)
      case boxPlot: BoxPlotDef =>
        new BoxPlotChart(getSize(boxPlot), boxPlot, boxPlot.options)
      case linePlot: LinePlotDef =>
        new LinePlot(getSize(linePlot), linePlot, linePlot.options)
    }
  }

}
