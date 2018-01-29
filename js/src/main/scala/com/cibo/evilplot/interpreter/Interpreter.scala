/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.interpreter
import com.cibo.evilplot.colors.Colors.ScaledColorBar
import com.cibo.evilplot.{JSONUtils, StrokeStyle, Style}
import com.cibo.evilplot.geometry.{Align, Beside, Disc, Drawable, Extent, Line}
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
  //scalastyle:off
  def eval(plotDef: PlotDef, extent: Option[Extent]): Drawable = {
    def getSize(pd: PlotDef): Extent = extent.getOrElse(pd.extent.getOrElse(defaultSize))
    plotDef match {
      case scatter: ScatterPlotDef => {
        val plot = ScatterPlot(getSize(scatter), scatter).drawable
        if (scatter.options.makeLegend && scatter.zData.isDefined) {
          plot beside Legend[Double](scatter.colorBar, scatter.zData.get.distinct, Disc(2, 0, 0), Style.apply).drawable
        } else plot
      }
      case contour: ContourPlotDef =>
        val plot = ContourPlot(getSize(contour), contour).drawable
        if (contour.options.makeLegend) contour.colorBar match {
          case scb: ScaledColorBar => plot beside GradientLegend(scb).drawable
          case _ => plot
        } else plot
      case xyPosterior: XYPosteriorPlotDef =>
        val plot = PosteriorPlot(getSize(xyPosterior), xyPosterior).drawable
        if (xyPosterior.options.makeLegend && xyPosterior.colorBar.isInstanceOf[ScaledColorBar])
          plot beside GradientLegend(xyPosterior.colorBar.asInstanceOf[ScaledColorBar]).drawable
        else plot
      case histogram: HistogramChartDef => HistogramChart(getSize(histogram), histogram).drawable
      case barChart: BarChartDef => BarChart(getSize(barChart), barChart).drawable
      case boxPlot: BoxPlotDef => BoxPlotChart(getSize(boxPlot), boxPlot).drawable
      case linePlot: LinePlotDef =>
        val plot = LinePlot(getSize(linePlot), linePlot).drawable
        if (linePlot.options.makeLegend) {
          val categories = linePlot.lines.flatMap(lpd => if (lpd.name.isDefined) Some(lpd.color, lpd.name.get) else None)
          val legend = Legend(
            ScaledColorBar(categories.map(_._1), 0, categories.length - 1),
            categories.map(_._2),
            Line(5, 2),
            StrokeStyle.apply
          ).drawable
          Align.middle(plot, legend) reduce Beside
        } else plot
      case facetsDef: FacetsDef => Facets(getSize(facetsDef), facetsDef).drawable
    }
  }

}
