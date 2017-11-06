/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.numeric

import org.scalactic.Tolerance


object AxisDescriptor {
  // Source for this value: my float.h header.
  private[numeric] val machineEpsilonIEEE754Double: Double = 2.220446e-16
  // Equal within tolerance test, tolerance is IEEE754 Double precision machine epsilon.
  private[numeric] def arePracticallyEqual(x: Double, y: Double): Boolean = {
    val diff = math.abs(x - y)
    diff < machineEpsilonIEEE754Double
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

case class AxisDescriptor(bounds: Bounds, numTicksRequested: Int, drawBounds: Option[Bounds] = None) {

  /** Given a numeric range and the desired number of ticks, figure out where to put the ticks so the labels will
    * have "nice" values (e.g., 100 not 137). Return the first tick, last tick, tick increment, and number of
    * fractional digits to show.
    * Note that the number of ticks fitting in the range might exceed the requested number.
    * See "Nice Numbers for Graph Labels" by Paul Heckbert, from "Graphics Gems", Academic Press, 1990.
    * This method implements "loose labeling", meaning that the minimum and maximum ticks are placed outside of
    * the bounds of the data.
    */
  val (maxValue, minValue) = (bounds.max, bounds.min)
  require(maxValue >= minValue || (bounds.max.isNaN && bounds.min.isNaN))
  private val range = AxisDescriptor.nicenum(maxValue - minValue, round = false)
  private[numeric] def calcSpacing(aRange: Double) =
    AxisDescriptor.nicenum(aRange / (numTicksRequested + 1), round = true)

  private val spacingGuess: Double = AxisDescriptor.nicenum(range / (numTicksRequested + 1), round = true)
  val (tickMin, tickMax, spacing) = {
    if (!AxisDescriptor.arePracticallyEqual(minValue, maxValue) && !(minValue.isNaN && maxValue.isNaN)) {
      (math.floor(minValue / spacingGuess) * spacingGuess,
        math.ceil(maxValue / spacingGuess) * spacingGuess,
        spacingGuess)
    } else
      (minValue - 0.5, minValue + 0.5, calcSpacing(1.0))
  }

  // Actual number of ticks generated.
  val numTicks: Int = ((tickMax - tickMin) / spacing).toInt + 1
  val axisBounds: Bounds = Bounds(tickMin, tickMax)

  // Avoid bad number formatting resulting from NaNs.
  val numFrac: Int = {
    if (!(tickMin.isNaN && tickMax.isNaN))
      math.max(-math.floor(math.log10(spacing)), 0).toInt
    else 0
  }
}


