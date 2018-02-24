package com.cibo.evilplot

import scala.language.implicitConversions
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._

package object numeric {
  type Grid = Vector[Vector[Double]]

  final case class Point(x: Double, y: Double) {
    def -(that: Point): Point = Point(x - that.x, y - that.y)
  }
  object Point {
    implicit val encoder: Encoder[Point] = io.circe.generic.semiauto.deriveEncoder[Point]
    implicit val decoder: Decoder[Point] = io.circe.generic.semiauto.deriveDecoder[Point]
    def tupled(t: (Double, Double)): Point = Point(t._1, t._2)
    implicit def toTuple(p: Point): (Double, Double) = (p.x, p.y)
  }

  case class Point3(x: Double, y: Double, z: Double)

  object Point3 {
    def tupled(t: (Double, Double, Double)): Point3 = Point3(t._1, t._2, t._3)
  }

  final case class GridData(grid: Grid,
                      xBounds: Bounds,
                      yBounds: Bounds,
                      zBounds: Bounds,
                      xSpacing: Double,
                      ySpacing: Double)

  object GridData {

    implicit val encoder: Encoder[GridData] = deriveEncoder[GridData]
    implicit val decoder: Decoder[GridData] = deriveDecoder[GridData]

    // make a grid data object out of a raw seq of points, assuming those points already lie in a grid.
    // the caller is presumed to know these parameters if they know that their data is already on a grid
    def apply(data: Seq[Point3],
              xSpacing: Double,
              ySpacing: Double,
              xBounds: Bounds,
              yBounds: Bounds): GridData = {
      val zBounds = Bounds(data.minBy(_.z).z, data.maxBy(_.z).z)
      val numCols: Int = (xBounds.range / xSpacing).toInt
      val numRows: Int = (yBounds.range / ySpacing).toInt
      val _grid: Array[Array[Double]] =
        Array.fill(numRows)(Array.fill(numCols) {
          0
        })
      for (Point3(x, y, z) <- data) {
        val row: Int =
          math.min(((y - yBounds.min) / ySpacing).toInt, numRows - 1)
        val col: Int =
          math.min(((x - xBounds.min) / xSpacing).toInt, numCols - 1)
        _grid(row)(col) = z
      }
      val grid: Grid = _grid.map(_.toVector).toVector
      GridData(grid, xBounds, yBounds, zBounds, xSpacing, ySpacing)
    }
  }

  case class Segment(a: Point, b: Point)

  object Segment {
    implicit val encoder: Encoder[Segment] = deriveEncoder[Segment]
    implicit val decoder: Decoder[Segment] = deriveDecoder[Segment]
  }

  final case class Bounds(min: Double, max: Double) {
    lazy val range: Double = max - min

    def isInBounds(x: Double): Boolean = x >= min && x <= max
  }

  object Bounds {

    implicit val encoder: Encoder[Bounds] = deriveEncoder[Bounds]
    implicit val decoder: Decoder[Bounds] = deriveDecoder[Bounds]

    private def lift[T](expr: => T): Option[T] = {
      try {
        Some(expr)
      } catch {
        case _: Exception => None
      }
    }


    def getBy[T](data: Seq[T])(f: T => Double): Option[Bounds] = {
      val mapped = data.map(f).filterNot(_.isNaN)
      for {
        min <- lift(mapped.min)
        max <- lift(mapped.max)
      } yield Bounds(min, max)
    }

    def get(data: Seq[Double]): Option[Bounds] = {
      data.foldLeft(None: Option[Bounds]) { (bounds, value) =>
        bounds match {
          case None => Some(Bounds(value, value))
          case Some(Bounds(min, max)) =>
            Some(Bounds(math.min(min, value), math.max(max, value)))
        }
      }
    }

    def widest(bounds: Seq[Option[Bounds]]): Option[Bounds] =
      bounds.flatten.foldLeft(None: Option[Bounds]) { (acc, curr) =>
        if (acc.isEmpty) Some(curr)
        else
          Some(
            Bounds(math.min(acc.get.min, curr.min),
                   math.max(acc.get.max, curr.max)))
      }
  }

  private val normalConstant = 1.0 / math.sqrt(2 * math.Pi)

  // with sigma = 1.0 and mu = 0, like R's dnorm.
  def probabilityDensityInNormal(x: Double): Double =
    normalConstant * math.exp(-math.pow(x, 2) / 2)

  // quantiles using linear interpolation.
  def quantile(data: Seq[Double], quantiles: Seq[Double]): Seq[Double] = {
    if (data.isEmpty) Seq.fill(quantiles.length)(Double.NaN)
    else {
      val length = data.length
      val sorted = data.sorted
      for {
        quantile <- quantiles
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
  }

  def quantile(data: Seq[Double], quantileDesired: Double): Double =
    quantile(data, Seq(quantileDesired)).head

  // yes, these functions require multiple passes through the data.
  def mean(data: Seq[Double]): Double = data.sum / data.length

  def variance(data: Seq[Double]): Double = {
    val _mean = mean(data)
    data.map(x => math.pow(x - _mean, 2)).sum / (data.length - 1)
  }

  def standardDeviation(data: Seq[Double]): Double = math.sqrt(variance(data))

}
