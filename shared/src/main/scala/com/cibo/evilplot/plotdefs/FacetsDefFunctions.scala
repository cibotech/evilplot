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
  def fixHistogramXBounds(hs: Seq[HistogramChartDef]): Seq[HistogramChartDef] = {
    val widestX = widestBounds(hs, xAxis)
    hs.map(h => h.copy(data = Histogram(h.data.rawData, h.data.numBins, widestX),
      options = h.options.copy(xAxisBounds = widestX)))
  }

  def fixBounds(axis: BoundsFunction)(pds: Seq[PlotDef]): Seq[PlotDef] = {
    val fixed = widestBounds(pds, axis)
    pds.map((pd: PlotDef) => pd.withOptions(pd.options.copy(yAxisBounds = fixed)))
  }

  def rowCol(indices: Iterable[Int], nCols: Int): Iterable[(Int, Int)] = indices.map(i => (i / nCols, i % nCols))
  type ConfigFunction = ((PlotDef, (Int, Int))) => PlotDef
  val xAxisOn: ConfigFunction = { case (pd, _) => pd.withOptions(pd.options.copy(drawXAxis = true)) }
  val xAxisOff: ConfigFunction = { case (pd, _) => pd.withOptions(pd.options.copy(drawXAxis = false)) }
  val yAxisOn: ConfigFunction = { case (pd, _) => pd.withOptions(pd.options.copy(drawYAxis = true)) }
  val yAxisOff: ConfigFunction = { case (pd, _) => pd.withOptions(pd.options.copy(drawYAxis = false)) }

  def bottomXLabels(nRows: Int, nCols: Int)(pds: Seq[PlotDef]): Seq[PlotDef] = {
    val defsWithRowsCols = pds.zip(rowCol(pds.indices, nCols))
    val inBottom: ((PlotDef, (Int, Int))) => Boolean = { case (_, rc) => rc._1 == nRows - 1 }
    defsWithRowsCols.map(pdrc => if (inBottom(pdrc)) xAxisOn(pdrc) else xAxisOff(pdrc))
  }

  def leftYLabels(nRows: Int, nCols: Int)(pds: Seq[PlotDef]): Seq[PlotDef] = {
    val defsWithRowsCols = pds.zip(rowCol(pds.indices, nCols))
    val atLeft: ((PlotDef, (Int, Int))) => Boolean = { case (_, rc) => rc._2 == 0 }
    defsWithRowsCols.map(pdrc => if (atLeft(pdrc)) yAxisOn(pdrc) else yAxisOff(pdrc))
  }
}
