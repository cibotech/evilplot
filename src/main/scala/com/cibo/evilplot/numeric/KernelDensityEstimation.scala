package com.cibo.evilplot.numeric

import com.cibo.evilplot.geometry.Point
import com.cibo.evilplot.plot.{Bounds, GridData}

// The geom_density_2d function in ggplot uses MASS:2dkde to interpolate a grid.
// We'll need to do 2D interpolation to generate  reports.
// We should be doing computations like this server side using either a cibo stats lib or something like Breeze,
// however this is a simple stand-in implementation.
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
    val (bandwidthX, bandwidthY) = (bandwidthEstimate(xs) / 4, bandwidthEstimate(ys) / 4)

    val xMatrix = kernelMatrix(data.map(_.x).toArray, _xBounds, numXs, bandwidthX)
    val yMatrix = kernelMatrix(data.map(_.y).toArray, _yBounds, numYs, bandwidthY)
    val estimate = matrixMatrixTransposeMult(yMatrix, xMatrix).map(_.map(_ / numXs * bandwidthX * bandwidthY))
    val zBounds = Bounds(estimate.map(_.min).min, estimate.map(_.max).max)
    println(estimate.length, estimate.head.length)
    assert(estimate.length == numXs && estimate.head.length == numYs,
    "density estimate dimensions do not match expectation")
    println(s"Finished KDE and zBounds are (${zBounds.min}, ${zBounds.max})")
    GridData(estimate.map(_.toVector).toVector, _xBounds, _yBounds, zBounds,
      _xBounds.range / numXs, _yBounds.range / numYs)
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

  private def kernelMatrix(vals: Array[Double], bounds: Bounds, nGridPoints: Int,
                           bandwidth: Double): Array[Array[Double]] = {
    val gridPoints = Array.tabulate(nGridPoints)(bounds.min + _ * bounds.range / nGridPoints)
    outerProduct(gridPoints, vals, (a: Double, b: Double) => probabilityDensityInNormal((a - b) / bandwidth))
  }

  private def outerProduct(a: Array[Double], b: Array[Double],
                           f: (Double, Double) => Double = _ * _): Array[Array[Double]] = {
    Array.tabulate(a.length, b.length) { (row, col) => f(a(row), b(col)) }
  }

  // lots of magic numbers, not sure on the theory behind this "rule of thumb" for bandwidth estimation.
  
  private def bandwidthEstimate(vec: Seq[Double]) = {
    val interQuartileRange = quantile(vec, Seq(0.25, 0.75))
         // someone who knows more about math might know what to call this val
    val approxThreeQuartersOfIQR = interQuartileRange.foldRight(0.0)(_ - _) / 1.34
    4 * 1.06 * math.min(standardDeviation(vec), approxThreeQuartersOfIQR) * math.pow(vec.length, -1.0 / 5.0)
  }
}
