/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.plotdefs

import com.cibo.evilplot.numeric.{Bounds, Histogram}

private[plotdefs] object FacetsDefFunctions {
  type BoundsFunction = PlotDef => Option[Bounds]
  val xAxis: BoundsFunction = pd => pd.xBounds
  val yAxis: BoundsFunction = pd => pd.yBounds
  /** From a sequence of PlotDefs optionally return the minimum low-bound and maximum max-bound of an axis. */
  def widestBounds(pds: Seq[PlotDef], axis: BoundsFunction): Option[Bounds] = {
    val allBounds = for {
      pd <- pds
      bounds <- axis(pd)
    } yield bounds
    if (allBounds.isEmpty) None else Some(Bounds(allBounds.minBy(_.min).min, allBounds.maxBy(_.max).max))
  }

  /** Create new HistogramChartDefs from rebinning data over the most extreme x axis bounds. */
  def fixHistogramXBounds(widest: Option[Bounds], h: HistogramChartDef): HistogramChartDef = {
    h.copy(data = Histogram(h.data.rawData, h.data.numBins, widest), options = h.options.copy(xAxisBounds = widest))
  }

  def fixXBounds(pds: Seq[PlotDef]): Seq[PlotDef] = {
    val fixed = widestBounds(pds, xAxis)
    pds.map {
      case hd: HistogramChartDef => fixHistogramXBounds(fixed, hd)
      case pd: PlotDef => pd.withOptions(pd.options.copy(xAxisBounds = fixed))
    }
  }

  def fixYBounds(pds: Seq[PlotDef]): Seq[PlotDef] = {
    val fixed = widestBounds(pds, yAxis)
    pds.map(pd => pd.withOptions(pd.options.copy(yAxisBounds = fixed)))
  }

  def rowCol(indices: Iterable[Int], nCols: Int): Iterable[(Int, Int)] = indices.map(i => (i / nCols, i % nCols))
  type ConfigFunction = ((PlotDef, (Int, Int))) => PlotDef
  val xAxisOn: ConfigFunction = { case (pd, _) => pd.withOptions(pd.options.copy(drawXAxis = true)) }
  val xAxisOff: ConfigFunction = { case (pd, _) => pd.withOptions(pd.options.copy(drawXAxis = false)) }
  val yAxisOn: ConfigFunction = { case (pd, _) => pd.withOptions(pd.options.copy(drawYAxis = true)) }
  val yAxisOff: ConfigFunction = { case (pd, _) => pd.withOptions(pd.options.copy(drawYAxis = false)) }


  def bottomXAxes(nRows: Int, nCols: Int)(pds: Seq[PlotDef]): Seq[PlotDef] = {
    val defsWithRowsCols = pds.zip(rowCol(pds.indices, nCols))
    val inBottom: ((PlotDef, (Int, Int))) => Boolean = { case (_, rc) => rc._1 == nRows - 1 }
    defsWithRowsCols.map(pdrc => if (inBottom(pdrc)) xAxisOn(pdrc) else xAxisOff(pdrc))
  }

  def leftYAxes(nRows: Int, nCols: Int)(pds: Seq[PlotDef]): Seq[PlotDef] = {
    val defsWithRowsCols = pds.zip(rowCol(pds.indices, nCols))
    val atLeft: ((PlotDef, (Int, Int))) => Boolean = { case (_, rc) => rc._2 == 0 }
    defsWithRowsCols.map(pdrc => if (atLeft(pdrc)) yAxisOn(pdrc) else yAxisOff(pdrc))
  }

  private def addColLabels(labels: Seq[String])(pdrc: (PlotDef, (Int, Int))): PlotDef = {
    val (pd, (row, col)) = pdrc
    if (row == 0 && labels.indices.contains(col))
      pd.withOptions(pd.options.copy(topLabel = Some(labels(col))))
    else pd
  }

  private def addRowLabels(nCols: Int)(labels: Seq[String])(pdrc: (PlotDef, (Int, Int))): PlotDef = {
    val (pd, (row, col)) = pdrc
    if (col == nCols - 1 && labels.indices.contains(row))
      pd.withOptions(pd.options.copy(rightLabel = Some(labels(row))))
    else pd
  }

  def addLabels(nRows: Int, nCols: Int, rowLabels: Option[Seq[String]], colLabels: Option[Seq[String]])
               (pds: Seq[PlotDef]): Seq[PlotDef] = {
    lazy val defsWithRowsCols: Seq[(PlotDef, (Int, Int))] = pds.zip(rowCol(pds.indices, nCols))
    (rowLabels, colLabels) match {
      case (None, None) => pds
      case (Some(rs), Some(cs)) => defsWithRowsCols.map(addRowLabels(nCols)(rs))
        .zip(rowCol(pds.indices, nCols)).map(addColLabels(cs))
      case (_, Some(cs)) => defsWithRowsCols.map(addColLabels(cs))
      case (Some(rs), _) => defsWithRowsCols.map(addRowLabels(nCols)(rs))
    }
  }
}
