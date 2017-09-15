package com.cibo.evilplot
import com.cibo.evilplot.SerializationUtils.withCaseClassConstructorName
package object numeric {
  type Grid = Vector[Vector[Double]]

  case class Point(x: Double, y: Double) {
    def - (that: Point): Point = Point(x - that.x, y - that.y) //scalastyle:ignore
  }
  case class Point3(x: Double, y: Double, z: Double)
  case class GridData(grid: Grid, xBounds: Bounds, yBounds: Bounds, zBounds: Bounds,
                      xSpacing: Double, ySpacing: Double)
  object GridData {
    // make a grid data object out of a raw seq of points, assuming those points already lie in a grid.
    // the caller is presumed to know these parameters if they know that their data is already on a grid
    def apply(data: Seq[Point3], xSpacing: Double, ySpacing: Double, xBounds: Bounds, yBounds: Bounds): GridData = {
      val zBounds = Bounds(data.minBy(_.z).z, data.maxBy(_.z).z)
      val numCols: Int = (xBounds.range / xSpacing).toInt
      val numRows: Int = (yBounds.range / ySpacing).toInt
      val _grid: Array[Array[Double]] = Array.fill(numRows)(Array.fill(numCols){0})
      for (Point3(x, y, z) <- data) {
        val row: Int = math.min(((y - yBounds.min) / ySpacing).toInt, numRows - 1)
        val col: Int = math.min(((x - xBounds.min) / xSpacing).toInt, numCols - 1)
        _grid(row)(col) = z
      }
      val grid: Grid = _grid.map(_.toVector).toVector
      GridData(grid, xBounds, yBounds, zBounds, xSpacing, ySpacing)
    }
  }

  case class Segment(a: Point, b: Point)

  case class Bounds(min: Double, max: Double) {
    lazy val range: Double = max - min

    def isInBounds(x: Double): Boolean = x >= min && x <= max
  }
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
