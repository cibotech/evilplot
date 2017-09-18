/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.interpreter
import com.cibo.evilplot.colors.Colors.SingletonColorBar
import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent}
import com.cibo.evilplot.plot.{ContourData, PlotOptions, ScatterPlotData}
import com.cibo.evilplot.plotdefs.{ContourPlotDef, PlotDef, ScatterPlotDef}
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
object PlotDefinitionInterpreter {
  val size = Extent(600, 600)
  def apply(definition: Json): Option[Drawable] = {
    evalPlotDef(definition)
    // Basically, figure out what it is this refers to, evaluate it, get the drawable.
  }

  private def evalPlotDef(plotDef: Json): Option[Drawable] = Some {
    EmptyDrawable()
    decode[PlotDef](plotDef.noSpaces).right.get match {
      case ScatterPlotDef(data, color) => ScatterPlotData(data, None, colorBar = SingletonColorBar(color)).createPlot(size, PlotOptions())
      case ContourPlotDef(gd, numContours) => ContourData(gd, numContours).createPlot(size, PlotOptions())
    }
  }

  private def evalFacetsDef(facetsDef: Json): Option[Drawable] = ???
}
