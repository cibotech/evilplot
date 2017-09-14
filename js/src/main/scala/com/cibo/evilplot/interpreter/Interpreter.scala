package com.cibo.evilplot.interpreter
import com.cibo.evilplot.geometry.Drawable
import io.circe._
object PlotDefinitionInterpreter {
  def apply(definition: Json): Option[Drawable] = {
    ???
    // Basically, figure out what it is this refers to, evaluate it, get the drawable.
  }

  private def evalPlotDef(plotDef: Json): Option[Drawable] = ???

  private def evalFacetsDef(facetsDef: Json): Option[Drawable] = ???
}
