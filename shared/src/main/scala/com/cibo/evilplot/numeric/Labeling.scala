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

object Labeling {
  private val defaultNTicks = 5
  private def defaultTickCountRange(center: Int): Seq[Int] = math.max(3, center / 2) to (2 * center)

  private val niceNums: Seq[Double] = Seq(1, 5, 2, 2.5, 3, 4, 1.5, 7, 6, 8, 9)

  /**
    * Implements a modified version of Wilkinson's labeling algorithm.
    * For reference, see ''The Grammar of Graphics'' (L. Wilkinson) pp. 96-97,
    * or ''An Extension to Wilkinson's Algorithm for Tick Labels on Axes'' (J. Talbot, et al.)
    * @param bounds The bounds of the data.
    * @param preferredTickCount A preferred number of ticks to use.
    * @param tickCountRange A function to create the range of acceptable tick counts. Labelings
    *                       with a tick count close to  `preferredTickCount` are favored,
    *                       however we may a choose a "nicer" labeling within this range.
    * @param formatter Function to format tick labels. If none is provided, we attempt to format
    *                  the tick labels using a reasonable amount of precision in decimal notation.
    * @param fixed When true, the generated labeling will never produces labels outside of the
    *              supplied bounds. In general better labelings can be achieved if fixed is not set.
    * @return A [[com.cibo.evilplot.numeric.AxisDescriptor]] representing the optimal labeling given
    *         the constraints if it exists, a naive labeling if none can be found.
    */
  def label(
    bounds: Bounds,
    preferredTickCount: Option[Int] = None,
    tickCountRange: Option[Seq[Int]] = None,
    formatter: Option[Double => String] = None,
    fixed: Boolean = false
  ): AxisDescriptor = {
    validate(bounds, preferredTickCount)
    val labelingType = if (fixed) LabelingType.StrictLabeling else LabelingType.LooseLabeling
    val ticksCenter = preferredTickCount.getOrElse(defaultNTicks)
    val ticksRange: Seq[Int] = tickCountRange.getOrElse(defaultTickCountRange(ticksCenter))

    if (AxisDescriptor.arePracticallyEqual(bounds.min, bounds.max)) {
      labelEqualBounds(bounds, ticksCenter, ticksRange, formatter, fixed)
    } else {
      (if (ticksCenter <= 2) Some(naiveFallback(bounds, ticksCenter, formatter)) else None)
        .orElse(if (bounds.max.isNaN && bounds.min.isNaN) Some(naiveFallback(bounds, 2, formatter))
        else None)
        .orElse(optimalLabeling(bounds, ticksCenter, ticksRange, niceNums, labelingType, formatter))
        .getOrElse(naiveFallback(bounds, ticksCenter, formatter))
    }
  }

  def forceTickCount(
    bounds: Bounds,
    tickCount: Int,
    formatter: Option[Double => String] = None,
    fixed: Boolean = false
  ): AxisDescriptor =
    label(
      bounds,
      preferredTickCount = Some(tickCount),
      tickCountRange = Some(Seq(tickCount)),
      formatter = formatter,
      fixed = fixed)

  private def labelEqualBounds(
    bounds: Bounds,
    preferredTickCount: Int,
    ticksRange: Seq[Int],
    formatter: Option[Double => String],
    fixed: Boolean): AxisDescriptor = {
    if (fixed)
      naiveFallback(bounds, 1, formatter)
    else {
      val bs = Bounds(bounds.min - 0.5, bounds.max + 0.5)
      optimalLabeling(
        bs,
        preferredTickCount,
        ticksRange,
        niceNums,
        LabelingType.LooseLabeling,
        formatter)
        .getOrElse(naiveFallback(bs, preferredTickCount, formatter))
    }
  }

  private def validate(bounds: Bounds, numTicks: Option[Int]): Unit = {
    require(
      bounds.max >= bounds.min || (bounds.max.isNaN && bounds.min.isNaN),
      "The maximum value must be greater than or equal to the minimum value.")
    require(numTicks.forall(_ >= 0), "Cannot use a negative number of ticks.")
  }

  private def optimalLabeling(
    bounds: Bounds,
    preferredTickCount: Int,
    ticksRange: Seq[Int],
    nicenums: Seq[Double],
    labelingType: LabelingType,
    formatter: Option[Double => String]
  ): Option[LabelingResult] = {
    ticksRange.foldLeft(None: Option[LabelingResult]) {
      case (bestScoring, numticks) =>
        val labeling =
          searchPowersAndIntervals(
            bounds,
            numticks,
            preferredTickCount,
            nicenums,
            labelingType,
            formatter)
        if (labeling.exists(l => bestScoring.forall(bs => l.score > bs.score))) labeling
        else bestScoring
    }
  }

  private def searchPowersAndIntervals(
    bounds: Bounds,
    tickCount: Int,
    preferredTickCount: Int,
    nicenums: Seq[Double],
    labelingType: LabelingType,
    formatter: Option[Double => String]
  ): Option[LabelingResult] = {
    val intervals = tickCount - 1
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
                val g = granularity(tickCount, preferredTickCount, 15)
                val c = coverage(bounds, labelMax, labelMin, labelingType)
                val s =
                  simplicity(i + 1, nicenums.length, if (labelMin <= 0 && labelMax >= 0) 1 else 0)
                val score: Double = cost(c, g, s)
                if (bestScoring.forall(best => score > best.score)) {
                  Some(
                    labelingType
                      .create(
                        bounds,
                        Bounds(labelMin, labelMax),
                        tickCount,
                        lDelta,
                        score,
                        extraPrecision(i, base),
                        formatter))
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
      score: Double,
      precisionBump: Int,
      formatter: Option[Double => String]): LabelingResult
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
        score: Double,
        precisionBump: Int,
        formatter: Option[Double => String]): LabelingResult = {
        LabelingResult(
          dataBounds,
          labelBounds,
          labelBounds,
          numTicks,
          spacing,
          score,
          precisionBump,
          formatter)
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
        score: Double,
        precisionBump: Int,
        formatter: Option[Double => String]): LabelingResult =
        LabelingResult(
          dataBounds,
          dataBounds,
          labelBounds,
          numTicks,
          spacing,
          score,
          precisionBump,
          formatter)
    }
  }

  private def naiveFallback(
    bs: Bounds,
    nticks: Int,
    formatter: Option[Double => String]): AxisDescriptor = new AxisDescriptor {
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
    private lazy val format: Double => String = formatter.getOrElse(_.toString)
    lazy val labels: Seq[String] = values.map(format)
  }

  private[numeric] case class LabelingResult(
    bounds: Bounds, // The bounds of the data.
    axisBounds: Bounds, // The bounds of the plot axis.
    labelBounds: Bounds, // The first and last label value.
    numTicks: Int,
    spacing: Double,
    score: Double,
    precisionBump: Int,
    formatter: Option[Double => String]
  ) extends AxisDescriptor {

    private lazy val format: Double => String = formatter.getOrElse {
      AxisDescriptor.createNumericLabel(_, precisionForBase(spacing) + precisionBump)
    }

    lazy val labels: Seq[String] = {
      values.map(format)
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
    (coverage + granularity + simplicity) / 3

  @inline private def granularity(k: Double, m: Double, allowedUpper: Double): Double =
    if (k > 0 && k < 2 * m)
      1 - math.abs(k - m) / m
    else 0

  // Hack to get an extra digit if we base the labeling off of 2.5 or 1.5.
  // But we still want 0 if we're using 25 or 15.
  private def extraPrecision(index: Int, base: Double): Int =
    if ((index == 3 || index == 6) && base <= 1) 1 else 0

  // Assumes (rightfully so for its use) that all the numbers it is going
  // to format are multiples of the spacing.
  private def precisionForBase(spacing: Double): Int = {
    if (spacing == 0 || spacing.isNaN) 0 else math.max(-math.floor(math.log10(spacing)), 0).toInt
  }
}
