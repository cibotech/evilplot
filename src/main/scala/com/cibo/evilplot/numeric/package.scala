package com.cibo.evilplot

package object numeric {
  private val normalConstant = 1.0 / math.sqrt(2 * math.Pi)
  // with sigma = 1.0 and mu = 0, like R's dnorm.
  def probabilityDensityInNormal(x: Double): Double = normalConstant * math.exp(-math.pow(x, 2) / 2)

  // quantiles using linear interpolation.
  def quantile(data: Seq[Double], quantiles: Seq[Double]): Seq[Double] = {
    val length = data.length
    val sorted = data.sorted
    for {quantile <- quantiles
         _ = require(quantile >= 0.0 && quantile <= 1.0)
         index = quantile * (length - 1)
         result = {
           if (index >= length - 1) sorted.last
           else {
             val lower = sorted(math.floor(index).toInt)
             val upper = sorted(math.ceil(index).toInt)
             lower + (upper - lower) * (index - math.floor(index))
           }
         }
    } yield result
  }

  def quantile(data: Seq[Double], quantileDesired: Double): Double = quantile(data, Seq(quantileDesired)).head

  // yes, these functions require multiple passes through the data.
  def mean (data: Seq[Double]): Double = data.sum / data.length
  def variance(data: Seq[Double]): Double = {
    val _mean = mean(data)
    data.map(x => math.pow(x - _mean, 2)).sum / (data.length - 1)
  }

  def standardDeviation(data: Seq[Double]): Double = math.sqrt(variance(data))

}
