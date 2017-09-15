package com.cibo.evilplot.plotdefs

object FacetsDef {
  // type bound is too tight temporarily
  def apply[T, U, V <: ScatterPlotDef](dataObject: T,
                     columns: Seq[(T => U)],
                     rows: Seq[(U => V)],
                     columnLabels: Option[Seq[String]] = None,
                     rowLabels: Option[Seq[String]] = None,
                     axisScales: ScaleOption = FixedScales,
                     baseOptions: PlotOptions = PlotOptions()): FacetsDef = {
    val defs = for (row <- rows; col <- columns) yield row(col(dataObject))
    FacetsDef(rows.length, columns.length, defs, baseOptions)
  }
}
//@JsonCodec
case class FacetsDef(numRows: Int, numCols: Int, defs: Seq[ScatterPlotDef], baseOptions: PlotOptions)
