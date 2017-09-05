/*
 * Copyright 2017 CiBO Technologies
 */

package com.cibo.evilplot.numeric

import com.cibo.evilplot.plot.Bounds
class Histogram(data: Seq[Double], numBins: Int, bounds: Option[Bounds] = None) {
  private val _bins: Array[Long] = Array.fill(numBins){0}

  /** Histogram bins: a sequence of counts */
  // Expose an immutable Seq, not the mutable Array. We don't want the caller to be able to change values.
  lazy val bins: Seq[Long] = _bins.toSeq

  private val sorted = data.sorted

  val (min, max) = bounds match {
    case Some(Bounds(_min, _max)) => (_min, _max)
    case None => (sorted.head, sorted.last)
  }

  /** width of histogram bin */
  val binWidth: Double = (max - min) / numBins

  // Assign each datum to a bin. Make sure that points at the end don't go out of bounds due to numeric imprecision.
  for (value <- sorted) {
    val bin: Int = math.min(math.round(math.floor((value - min) / binWidth)).toInt, numBins - 1)
    _bins(bin) = _bins(bin) + 1
  }

  override def toString: String =
    f"Histogram with bin boundaries: $min%.3f $max%.3f, number of bins $numBins%d and binWidth $binWidth%.1f"
}
