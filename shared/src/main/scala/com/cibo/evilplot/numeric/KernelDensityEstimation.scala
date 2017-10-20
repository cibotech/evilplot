/*
 * Copyright 2017 CiBO Technologies
 */

package com.cibo.evilplot.numeric

// The geom_density_2d function in ggplot uses MASS:2dkde to interpolate a grid.
// We'll need to do 2D interpolation to generate  reports.
object KernelDensityEstimation {

  /**
    * Perform a 2-dimensional kernel density estimation on data.
    * @param data A sequence of points, for which a density estimate is to be generated. "x" corresponds to
    *             columns of the resulting grid, "y" to rows.
    * @param numPoints number of rows and number of columns of the resulting grid. Defaults to 100 x 100.
    * @param xBounds desired boundaries of the grid in data coordinates. Defaults to min and max x value of `data`.
    * @param yBounds desired boundaries of the grid in data coordinates. Defaults to min and max y value of `data`.
    * @return A GridData object containing the 2D density estimation.
    */

  def densityEstimate2D(data: Seq[Point], numPoints: (Int, Int) = (100, 100), xBounds: Option[Bounds] = None,
                        yBounds: Option[Bounds] = None): GridData = {
    val _xBounds = xBounds.getOrElse(Bounds(data.minBy(_.x).x, data.maxBy(_.x).x))
    val _yBounds = yBounds.getOrElse(Bounds(data.minBy(_.y).y, data.maxBy(_.y).y))
    val (numXs, numYs) = numPoints
    val (xs, ys) = (data.map(_.x).toArray, data.map(_.y).toArray)
    val (bandwidthX, bandwidthY) = (bandwidthEstimate(xs) / 4.0, bandwidthEstimate(ys) / 4.0)
    val (spacingX, spacingY) = (_xBounds.range / (numXs - 1), _yBounds.range / (numYs - 1))

    val xMatrix = kernelMatrix(data.map(_.x).toArray, _xBounds, numXs, spacingX, bandwidthX)
    val yMatrix = kernelMatrix(data.map(_.y).toArray, _yBounds, numYs, spacingY, bandwidthY)
    val estimate = matrixMatrixTransposeMult(xMatrix, yMatrix).map(_.map(_ / (data.length * bandwidthX * bandwidthY)))
    val zBounds = Bounds(estimate.map(_.min).min, estimate.map(_.max).max)
    assert(estimate.length == numXs && estimate.head.length == numYs,
    "density estimate dimensions do not match expectation")
    GridData(estimate.map(_.toVector).toVector, _xBounds, _yBounds, zBounds, spacingX, spacingY)
  }

  def matrixMatrixTransposeMult(a: Array[Array[Double]], b: Array[Array[Double]]): Array[Array[Double]] = {
    require(a.head.length == b.head.length, "matrix multiplication is not defined for matrices whose" +
      " inner dimensions are not equal")
    val result: Array[Array[Double]] = Array.fill(a.length, b.length) { 0.0 }
    for (i <- a.indices; j <- a.indices; k <- a.head.indices) {
      result(i)(j) = result(i)(j) + a(i)(k) * b(j)(k)
    }
    result
  }

  private def kernelMatrix(vals: Array[Double], bounds: Bounds, nGridPoints: Int, spacing: Double,
                           bandwidth: Double): Array[Array[Double]] = {
    val gridPoints = Array.tabulate(nGridPoints)(bounds.min + _ * spacing)
    outerProduct(gridPoints, vals, (a: Double, b: Double) => probabilityDensityInNormal((a - b) / bandwidth))
  }

  private[numeric] def outerProduct(a: Array[Double], b: Array[Double],
                                   f: (Double, Double) => Double = _ * _): Array[Array[Double]] = {
    Array.tabulate(a.length, b.length) { (row, col) => f(a(row), b(col)) }
  }

  // lots of magic numbers, not sure on the theory behind this "rule of thumb" for bandwidth estimation.
  
  
  private[numeric] def bandwidthEstimate(vec: Seq[Double]) = {
    val iqr = quantile(vec, Seq(0.25, 0.75)).reduceLeft((first, third) => third - first)
    val h = iqr / 1.34
    4 * 1.06 * math.min(standardDeviation(vec), h) * math.pow(vec.length, -1.0 / 5.0)
  }
}
