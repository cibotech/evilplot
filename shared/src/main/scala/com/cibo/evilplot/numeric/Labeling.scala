/*
 * Copyright (c) 2018, CiBO Technologies, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.cibo.evilplot.numeric

import scala.util.control.NonFatal

object Labeling {
  private val defaultNTicks = 5
  private val defaultMRange = 2 to 10

  /**
    * Implements a modified version of Wilkinson's labeling algorithm.
    * For reference, see ''The Grammar of Graphics'' (L. Wilkinson) pp. 96-97,
    * or ''An Extension to Wilkinson's Algorithm for Tick Labels on Axes'' (J. Talbot, et al.)
    * @param bounds The bounds of the data.
    * @param numTicks Optional number of ticks to use. If provided, the resulting
    *               axis will have exactly that number of ticks. By default,
    *               we optimize for axis "goodness" over a range of 3 to 12 ticks.
    * @param nicenums A list of intervals which are considered to be "nice" by humans,
    *                 in the order from most preferred to least. We base our spacings
    *                 based on the powers of ten of these numbers.
    * @param fixed When true, the generated labeling will never produces labels outside of the
    *              supplied bounds. In general better labelings can be achieved if fixed is not set.
    * @return A [[com.cibo.evilplot.numeric.AxisDescriptor]] representing the optimal labeling given
    *         the constraints if it exists, a naive labeling if none can be found.
    * @note Given the default nice number list and number of ticks search space,
    *       this method should give a result. It is only when `numTicks` is constrained
    *       to a single value and the `nicenums` list is shortened that there exists a risk
    *       of no result being found.
    */
  def label(
    bounds: Bounds,
    numTicks: Option[Int] = None,
    nicenums: Seq[Double] = Seq(1, 5, 2, 2.5, 3, 4, 1.5, 7, 6, 8, 9),
    fixed: Boolean = false
  ): AxisDescriptor = {
    validate(bounds, numTicks, nicenums)
    val labelingType = if (fixed) LabelingType.StrictLabeling else LabelingType.LooseLabeling

    if (AxisDescriptor.arePracticallyEqual(bounds.min, bounds.max)) {
      labelEqualBounds(bounds, numTicks, defaultNTicks, nicenums, fixed)
    } else {
      numTicks
        .flatMap(nt => if (nt <= 2) Some(naiveFallback(bounds, nt)) else None)
        .orElse(if (bounds.max.isNaN && bounds.min.isNaN) Some(naiveFallback(bounds, 2)) else None)
        .orElse(optimalLabeling(bounds, numTicks, defaultNTicks, nicenums, labelingType))
        .getOrElse(naiveFallback(bounds, numTicks.getOrElse(defaultNTicks)))
    }
  }

  private def labelEqualBounds(
    bounds: Bounds,
    numTicks: Option[Int],
    targetNTicks: Int,
    nicenums: Seq[Double],
    fixed: Boolean): AxisDescriptor = {
    if (fixed)
      naiveFallback(bounds, 1)
    else {
      val bs = Bounds(bounds.min - 0.5, bounds.max + 0.5)
      optimalLabeling(bs, numTicks, targetNTicks, nicenums, LabelingType.LooseLabeling)
        .getOrElse(naiveFallback(bs, numTicks.getOrElse(targetNTicks)))
    }
  }

  private def validate(bounds: Bounds, numTicks: Option[Int], nicenums: Seq[Double]): Unit = {
    require(nicenums.nonEmpty, "The list of preferred intervals must not be empty.")
    require(
      bounds.max >= bounds.min || (bounds.max.isNaN && bounds.min.isNaN),
      "The maximum value must be greater than or equal to the minimum value.")
    require(numTicks.forall(_ >= 0), "Cannot use a negative number of ticks.")
  }

  private def optimalLabeling(
    bounds: Bounds,
    nticks: Option[Int],
    targetnticks: Int,
    nicenums: Seq[Double],
    labelingType: LabelingType
  ): Option[LabelingResult] = {
    val mrange = nticks.fold(defaultMRange: Seq[Int])(Seq(_))
    mrange.foldLeft(None: Option[LabelingResult]) {
      case (bestScoring, numticks) =>
        val labeling =
          searchPowersAndIntervals(bounds, numticks, targetnticks, nicenums, labelingType)
        if (labeling.exists(l => bestScoring.forall(bs => l.score > bs.score))) labeling
        else bestScoring
    }
  }

  private def searchPowersAndIntervals(
    bounds: Bounds,
    nticks: Int,
    targetnticks: Int,
    nicenums: Seq[Double],
    labelingType: LabelingType
  ): Option[LabelingResult] = {
    val intervals = nticks - 1
    val delta = bounds.range / intervals
    val power = math.floor(math.log10(delta)).toInt

    labelingType
      .powerSearchRange(power)
      .foldLeft(None: Option[LabelingResult]) {
        case (acc, pow) =>
          val base = math.pow(10, pow)
          nicenums.zipWithIndex.foldLeft(acc) {
            case (bestScoring, (q, i)) =>
              val lDelta = q * base
              val labelMin = labelingType.labelMin(bounds.min, lDelta)
              val labelMax = labelMin + intervals * lDelta
              if (labelingType.ofType(bounds, labelMin, labelMax, lDelta)) {
                val g = granularity(nticks, targetnticks, 15)
                val c = coverage(bounds, labelMax, labelMin, labelingType)
                val s =
                  simplicity(i + 1, nicenums.length, if (labelMin <= 0 && labelMax >= 0) 1 else 0)
                val score: Double = cost(c, g, s)
                if (bestScoring.forall(best => score > best.score)) {
                  Some(
                    labelingType
                      .create(bounds, Bounds(labelMin, labelMax), nticks, lDelta, score))
                } else {
                  bestScoring
                }
              } else bestScoring
          }
      }
  }

  private sealed trait LabelingType {
    def labelMin(min: Double, ldelta: Double): Double
    def ofType(dataBounds: Bounds, labelMin: Double, labelMax: Double, spacing: Double): Boolean
    def powerSearchRange(power: Int): Range.Inclusive
    def create(
      dataBounds: Bounds,
      labelBounds: Bounds,
      numTicks: Int,
      spacing: Double,
      score: Double): LabelingResult
  }

  // a "loose" labeling is one where the labels are either the bounds of the data or outside
  private object LabelingType {
    case object LooseLabeling extends LabelingType {
      def labelMin(min: Double, ldelta: Double): Double =
        math.floor(min / ldelta) * ldelta

      def powerSearchRange(power: Int): Range.Inclusive = power.to(power + 1)

      def ofType(dataBounds: Bounds, labelMin: Double, labelMax: Double, spacing: Double): Boolean =
        labelMin <= dataBounds.min && labelMax >= dataBounds.max

      def create(
        dataBounds: Bounds,
        labelBounds: Bounds,
        numTicks: Int,
        spacing: Double,
        score: Double): LabelingResult = {
        LabelingResult(dataBounds, labelBounds, labelBounds, numTicks, spacing, score)
      }
    }

    // a "strict" labeling is one where the labels are either the bounds of the data or inside
    case object StrictLabeling extends LabelingType {
      def labelMin(min: Double, ldelta: Double): Double =
        math.ceil(min / ldelta) * ldelta

      // Expand search range for strict labeling to increase chances of finding a labeling.
      def powerSearchRange(power: Int): Range.Inclusive = (power - 1).to(power + 1)

      // Must be strict _and_ complete, (can't fit another tick on either side)
      def ofType(
        dataBounds: Bounds,
        labelMin: Double,
        labelMax: Double,
        spacing: Double): Boolean = {
        labelMin >= dataBounds.min &&
        labelMax <= dataBounds.max &&
        labelMin - spacing <= dataBounds.min &&
        labelMax + spacing >= dataBounds.max
      }

      def create(
        dataBounds: Bounds,
        labelBounds: Bounds,
        numTicks: Int,
        spacing: Double,
        score: Double): LabelingResult =
        LabelingResult(dataBounds, dataBounds, labelBounds, numTicks, spacing, score)
    }
  }

  private def naiveFallback(bs: Bounds, nticks: Int): AxisDescriptor = new AxisDescriptor {
    val bounds: Bounds = bs
    val numTicks: Int = nticks
    val axisBounds: Bounds = bs

    lazy val values: Seq[Double] = {
      if (nticks == 0) Seq()
      else if (nticks == 1) Seq((bs.min + bs.max) / 2)
      else if (nticks == 2) Seq(bs.min, bs.max)
      else {
        val intervals = numTicks - 1
        val spacing = bounds.range / intervals
        Seq.tabulate(numTicks)(i => bounds.min + i * spacing)
      }
    }

    lazy val labels: Seq[String] = values.map(_.toString)
  }

  private[numeric] case class LabelingResult(
    bounds: Bounds, // The bounds of the data.
    axisBounds: Bounds, // The bounds of the plot axis.
    labelBounds: Bounds, // The first and last label value.
    numTicks: Int,
    spacing: Double,
    score: Double
  ) extends AxisDescriptor {

    private lazy val nfrac = math.max(-math.floor(math.log10(spacing)), 0).toInt

    lazy val labels: Seq[String] = {
      values.map(AxisDescriptor.createNumericLabel(_, nfrac))
    }

    lazy val values: Seq[Double] = Seq.tabulate(numTicks)(i => spacing * i + labelBounds.min)
  }

  @inline private def simplicity(i: Double, niceCount: Int, v: Double): Double =
    1 - (i / niceCount) + (v / niceCount)

  // For strict labelings we are trying to maximize the reciprocal of coverage.
  @inline private def coverage(
    dataBounds: Bounds,
    labelMax: Double,
    labelMin: Double,
    labelType: LabelingType): Double = {
    labelType match {
      case LabelingType.LooseLabeling  => dataBounds.range / (labelMax - labelMin)
      case LabelingType.StrictLabeling => (labelMax - labelMin) / dataBounds.range
    }
  }

  @inline private def cost(coverage: Double, granularity: Double, simplicity: Double): Double =
    (0.4 * coverage) + (0.2 * granularity) + (0.4 * simplicity)

  @inline private def granularity(k: Double, m: Double, allowedUpper: Double): Double =
    if (k > 0 && k < 2 * m)
      1 - math.abs(k - m) / m
    else 0
}