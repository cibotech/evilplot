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
  private val defaultMRange = 2 to 10

  /**
    * Implements a modified version of Wilkinson's labeling algorithm.
    * For reference, see ''The Grammar of Graphics'' (L. Wilkinson) pp. 96-97,
    * or ''An Extension to Wilkinson's Algorithm for Tick Labels on Axes'' (J. Talbot, et al.)
    * @param bounds The bounds of the data.
    * @param numTicks Optional number of ticks to use. If provided, the resulting
    *               axis will have exactly that number of ticks. By default,
    *               we optimize for axis "goodness" over a range of number of ticks.
    *               If "loose" is false the number of ticks will be no larger than nticks
    *               but _may_ be smaller.
    * @param nicenums A list of intervals which are considered to be "nice" by humans,
    *                 in the order from most preferred to least. We base our spacings
    *                 based on the powers of ten of these numbres.
    * @param fixed When true, the generated labeling will never produces labels outside of the
    *              supplied bounds. In general better labelings can be achieved if fixed is not set.
    */
  def label(
    bounds: Bounds,
    numTicks: Option[Int] = None,
    nicenums: Seq[Double] = Seq(1, 5, 2, 2.5, 3, 4, 1.5, 7, 6, 8, 9),
    fixed: Boolean = false
  ): AxisDescriptor = {
    validate(bounds, numTicks, nicenums)
    val labelingType = if (fixed) LabelingType.StrictLabeling else LabelingType.LooseLabeling
    val answer =
      if (numTicks.exists(_ <= 2)) fallback(bounds, numTicks.get)
      else if (bounds.max.isNaN && bounds.min.isNaN) fallback(bounds, 2)
      else if (AxisDescriptor.arePracticallyEqual(bounds.min, bounds.max))
        optimalLabeling(
          Bounds(bounds.min - 0.5, bounds.max + 0.5),
          numTicks,
          nicenums,
          labelingType)
      else optimalLabeling(bounds, numTicks, nicenums, labelingType)

    try {
      println(s"""
           |AXIS BOUNDS: ${answer.axisBounds}
           |DATA BOUNDS: ${answer.bounds}
           |SCORE: ${answer.asInstanceOf[LabelingResult].score}
           |VALUES: ${answer.values.mkString("[", ", ", "]")}
           |LABELS: ${answer.labels.mkString("[", ", ", "]")}
       """.stripMargin)

    } catch {
      case e: ClassCastException => ()
    }
    answer
  }

  private def validate(bounds: Bounds, numTicks: Option[Int], nicenums: Seq[Double]): Unit = {
    require(nicenums.nonEmpty, "The list of preferred intervals must not be empty.")
    require(
      bounds.max >= bounds.min || (bounds.max.isNaN && bounds.min.isNaN),
      "The maximum value must be greater than or equal to the minimum value.")
    require(numTicks.forall(_ >= 0), "Cannot use a negative number of ticks.")
  }

  /* Terminology used herein:
   * a "loose" labeling is one where the labels are either the bounds of the data or outside
   * a "strict" labeling is one where the labels are either the bounds of the data or inside
   * In particular, it is important to note that some loose labelings are strict labelings!
   */

  private def optimalLabeling(
    bounds: Bounds,
    nticks: Option[Int],
    nicenums: Seq[Double],
    labelingType: LabelingType
  ): LabelingResult = {
    val mrange = nticks.fold(defaultMRange: Seq[Int])(Seq(_))
    mrange.foldLeft(LabelingResult(bounds, bounds, bounds, 1, 1.0, 0.0)) { (bestScoring, nticks) =>
      val l = genlabels(bounds, nticks, nicenums, labelingType)
      if (l.forall(_.score > bestScoring.score)) l.getOrElse(bestScoring)
      else bestScoring
    }
  }

  private def genlabels(
    bounds: Bounds,
    nticks: Int,
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
              if (labelingType.ofType(bounds, labelMin, labelMax)) {
                val g = granularity(nticks, nticks)
                val c = coverage(bounds, labelMax, labelMin, labelingType)
                val s =
                  simplicity(i + 1, nicenums.length, if (labelMin <= 0 && labelMax >= 0) 1 else 0)
                val score = (c + g + s) / 3
                val improved = bestScoring.forall(_.score < score) && score <= 1.0
                if (improved) {
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

  private[numeric] sealed trait LabelingType {
    def labelMin(min: Double, ldelta: Double): Double
    def ofType(dataBounds: Bounds, labelMin: Double, labelMax: Double): Boolean
    def powerSearchRange(power: Int): Range.Inclusive
    def create(
      dataBounds: Bounds,
      labelBounds: Bounds,
      numTicks: Int,
      spacing: Double,
      score: Double): LabelingResult
  }
  private[numeric] object LabelingType {
    case object LooseLabeling extends LabelingType {
      def labelMin(min: Double, ldelta: Double): Double =
        math.floor(min / ldelta) * ldelta

      def powerSearchRange(power: Int): Range.Inclusive = power.to(power + 1)

      def ofType(dataBounds: Bounds, labelMin: Double, labelMax: Double): Boolean =
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

    case object StrictLabeling extends LabelingType {
      def labelMin(min: Double, ldelta: Double): Double =
        math.ceil(min / ldelta) * ldelta

      // Expand search range for strict labeling to increase chances of finding a labeling.
      def powerSearchRange(power: Int): Range.Inclusive = (power - 1).to(power + 1)

      def ofType(dataBounds: Bounds, labelMin: Double, labelMax: Double): Boolean =
        labelMin >= dataBounds.min && labelMax <= dataBounds.max

      def create(
        dataBounds: Bounds,
        labelBounds: Bounds,
        numTicks: Int,
        spacing: Double,
        score: Double): LabelingResult =
        LabelingResult(dataBounds, dataBounds, labelBounds, numTicks, spacing, score)

    }
  }

  private def fallback(bs: Bounds, nticks: Int): AxisDescriptor = new AxisDescriptor {
    val bounds: Bounds = bs
    val numTicks: Int = nticks
    val axisBounds: Bounds = bs

    lazy val values: Seq[Double] = {
      if (nticks == 0) Seq()
      else if (nticks == 1) Seq((bs.min + bs.max) / 2)
      else if (nticks == 2) Seq(bs.min, bs.max)
      else throw new IllegalArgumentException("Fallback axis descriptor only handles 0 - 2 ticks")
    }

    lazy val labels: Seq[String] = values.map(_.toString)
  }

  private[numeric] case class LabelingResult(
    bounds: Bounds, // The bounds of the data.
    axisBounds: Bounds, // The bounds of the plot axis.
    labelBounds: Bounds, // The first and last label value.
    numTicks: Int,
    spacing: Double,
    score: Double,
    loose: Boolean = true
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

  @inline private def granularity(k: Double, m: Double): Double =
    if (k > 0 && k < 2 * m) 1 - math.abs(k - m) / m else 0
}
