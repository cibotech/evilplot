/*
 * Copyright 2017 CiBO Technologies
 */

package com.cibo.evilplot.numeric

// The underlying raw data does not get serialized. This is problematic: deserializing a Histogram
// will always give you an empty Seq.
case class Histogram(bins: Seq[Long], numBins: Int, binWidth: Double, min: Double, max: Double, rawData: Seq[Double])

object Histogram {
  def apply(data: Seq[Double], numBins: Int, bounds: Option[Bounds] = None): Histogram = {
    val _bins: Array[Long] = Array.fill(numBins) { 0 }

    val sorted = data.sorted

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

    Histogram(_bins.toSeq, numBins, binWidth, min, max, data)
  }
}

