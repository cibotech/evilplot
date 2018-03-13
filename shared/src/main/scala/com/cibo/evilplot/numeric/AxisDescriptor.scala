/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.numeric

trait AxisDescriptor {
  val bounds: Bounds
  val numTicks: Int
  val axisBounds: Bounds
  val labels: Seq[String]
  val values: Seq[Double]
}

case class DiscreteAxisDescriptor(_ticks: Seq[(String, Double)]) extends AxisDescriptor {
  val ticks: Seq[(String, Double)] = _ticks.sortBy(_._2)
  val labels: Seq[String] = ticks.map(_._1)
  val values: Seq[Double] = ticks.map(_._2)
  val bounds: Bounds = if (values.nonEmpty) Bounds(values.min, values.max + 1) else Bounds(0, 0)
  val numTicks: Int = values.length
  val axisBounds: Bounds = bounds
}

case class ContinuousAxisDescriptor(
  bounds: Bounds,
  numTicksRequested: Int,
  fixed: Boolean
) extends AxisDescriptor {

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
    if (fixed) {
      (minValue, maxValue, spacingGuess)
    } else if (!AxisDescriptor.arePracticallyEqual(minValue, maxValue) && !(minValue.isNaN && maxValue.isNaN)) {
      (math.floor(minValue / spacingGuess) * spacingGuess,
        math.ceil(maxValue / spacingGuess) * spacingGuess,
        spacingGuess)
    } else {
      (minValue - 0.5, minValue + 0.5, calcSpacing(1.0))
    }
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

  lazy val values: Seq[Double] = (0 until numTicks).map { i =>
    axisBounds.min + i * spacing
  }

  lazy val labels: Seq[String] = values.map { value =>
    AxisDescriptor.createNumericLabel(value, numFrac)
  }
}

object AxisDescriptor {
  private[numeric] val machineEpsilonIEEE754Double: Double = math.ulp(1.0)

  // Equal within tolerance test, tolerance is IEEE754 Double precision machine epsilon.
  def arePracticallyEqual(x: Double, y: Double): Boolean = {
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

  def createNumericLabel(num: Double, numFrac: Int): String = {
    require(numFrac >= 0 && numFrac <= 20, "Formatting fewer than 0" +
      s"or more than 20 decimal places is unsupported, but you attempted to format with $numFrac")
    val fmtString = "%%.%df".format(numFrac)
    fmtString.format(num)
  }
}


