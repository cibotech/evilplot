/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.numeric

object Ticks {

  /** Given a numeric range and the desired number of ticks, figure out where to put the ticks so the labels will
    * have "nice" values (e.g., 100 not 137). Return the first tick, last tick, tick increment, and number of
    * fractional digits to show.
    * Note that the number of ticks fitting in the range might exceed the requested number.
    * See "Nice Numbers for Graph Labels" by Paul Heckbert, from "Graphics Gems", Academic Press, 1990.
    * This method implements "loose labeling", meaning that the min and max values in the graph are not labeled
    * if they are not nice values.
    */
  def niceTicks(minValue: Double, maxValue: Double, numTicks: Int): (Double, Double, Double, Int) = {
    require(maxValue > minValue)
    val range = nicenum(maxValue - minValue, false)
    // Modify Heckbert's algorithm in order to yield tick marks inside the range.
    // Heckbert uses (numTicks - 1) in order to have that much spacing between numTicks ticks. Crank that up.
    val spacing = nicenum(range / (numTicks + 1), true)
    val tickMin = math.ceil(minValue / spacing) * spacing
    val tickMax = math.floor(maxValue / spacing) * spacing
    val numFrac = math.max(- math.floor(math.log10(spacing)), 0).toInt
    (tickMin, tickMax, spacing, numFrac)
  }

  /** Find a "nice" number approximately equal to x. Round the number if round == 1, take ceiling if round == 0. */
  private[numeric] def nicenum(x: Double, round: Boolean): Double = {
    val expv = math.floor(math.log10(x))
    val f = x / math.pow(10, expv)                // between 1 and 10
    val nf =
      if (round) {
        if (f < 1.5) 1
        else if (f < 3) 2
        else if (f < 7) 5
        else 10.0
      }
      else {
        if (f <= 1) 1
        else if (f <= 2) 2
        else if (f <= 5) 5
        else 10
      }

    nf * math.pow(10, expv)
  }

}
