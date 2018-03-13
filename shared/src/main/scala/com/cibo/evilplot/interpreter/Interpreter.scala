/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.interpreter
import com.cibo.evilplot.JSONUtils
import com.cibo.evilplot.geometry._
import com.cibo.evilplot.plotdefs._

/** If an extent is supplied to the PlotDefinitionInterpreter, the serialized plot extent is overridden. */
//todo: get rid of with PlotDef's
object PlotDefinitionInterpreter {
  val defaultSize = Extent(800, 400) // completely arbitrary, can change later.
  def apply(definition: String, extent: Option[Extent] = None): Drawable = {
    val plotDef = JSONUtils.decodeStr[PlotDef](definition)
    eval(plotDef, extent)
  }
  //scalastyle:off
  def eval(plotDef: PlotDef, extent: Option[Extent]): Drawable = {
    new EmptyDrawable
  }

}
