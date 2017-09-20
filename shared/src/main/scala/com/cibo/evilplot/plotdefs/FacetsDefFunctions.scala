/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.plotdefs

import com.cibo.evilplot.numeric.{Bounds, Histogram}

// PlotDef.scala is too big but the companion object needs to be in the same file. Put helper methods here for dev
// and move back later if you want.
private[plotdefs] object FacetsDefFunctions {
  /** From a sequence of PlotDefs optionally return the minimum low-bound and maximum max-bound of an axis. */
  def widestBounds(pds: Seq[PlotDef], axis: (PlotDef => Option[Bounds])): Option[Bounds] = {
    val allBounds = for {
      pd <- pds
      bounds <- axis(pd)
    } yield bounds
    if (allBounds.isEmpty) None else Some(Bounds(allBounds.minBy(_.min).min, allBounds.maxBy(_.max).max))
  }

  /** Create new HistogramChartDefs from rebinning data over the most extreme x axis bounds. */
  def fixHistogramXBounds(hs: Seq[HistogramChartDef]): Seq[HistogramChartDef] = {
    val widestX = widestBounds(hs, h => h.xBounds)
    hs.map(h => h.copy(data = Histogram(h.data.rawData, h.data.numBins, widestX)))
  }
}
