/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.interpreter
import com.cibo.evilplot.Text
import com.cibo.evilplot.geometry.Drawable
import com.cibo.evilplot.plotdefs._
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._

object PlotDefinitionInterpreter {
  def apply(definition: String): Either[Error, Drawable] = {
    decode[PlotDef](definition).right.map { right: PlotDef => eval(right) }
  }

  private def eval(plotDef: PlotDef): Drawable = {
    plotDef match {
      case _ => Text("This feature is unimplemented.")
    }
  }

}
