/*
 * Copyright 2017 CiBO Technologies
 */

package com.cibo.evilplot.plotdefs

object FacetsDef {
  // type bound is too tight temporarily
  def apply[T, U, V <: PlotDef](dataObject: T,
                     columns: Seq[(T => U)],
                     rows: Seq[(U => V)],
                     columnLabels: Option[Seq[String]] = None,
                     rowLabels: Option[Seq[String]] = None,
                     axisScales: ScaleOption = FixedScales,
                     baseOptions: PlotOptions = PlotOptions()): FacetsDef = {
    val defs = for (row <- rows; col <- columns) yield row(col(dataObject))
    FacetsDef(rows.length, columns.length, defs, columnLabels, rowLabels, axisScales, baseOptions)
  }
}
//@JsonCodec
case class FacetsDef(numRows: Int, numCols: Int, defs: Seq[PlotDef], columnLabels: Option[Seq[String]],
                     rowLabels: Option[Seq[String]], axisScales: ScaleOption, baseOptions: PlotOptions)
