/*
 * Copyright 2017 CiBO Technologies
 */

package com.cibo.evilplot.oldplot

import com.cibo.evilplot.colors._
import com.cibo.evilplot.geometry.{AffineTransform, Disc, Drawable, EmptyDrawable, Extent, Path, StrokeStyle}
import com.cibo.evilplot.numeric.{Bounds, MarchingSquares, Point}
import com.cibo.evilplot.plotdefs.XYPosteriorPlotDef

case class PosteriorPlot(chartSize: Extent, data: XYPosteriorPlotDef) extends Chart with ContinuousAxes {
  val options = data.options
  private val numContours = data.numContours
  private val grid = data.gridData
  val defaultXAxisBounds: Bounds = data.xBounds.get
  val defaultYAxisBounds: Bounds = data.yBounds.get

  def plottedData(extent: Extent): Drawable = {
    val xBounds = xAxisDescriptor.axisBounds
    val yBounds = yAxisDescriptor.axisBounds
    val affine = AffineTransform(shiftX = -xBounds.min).scale(x = extent.width / xBounds.range)
        .compose(AffineTransform(scaleY = -1).translate(dx = 0, dy = yBounds.max)
          .scale(y = extent.height / yBounds.range))

    val colorBar = data.colorBar
    val binWidth = data.zBounds.range / numContours
    val levels = Seq.tabulate[Double](numContours - 1)(bin =>
      grid.zBounds.min + (bin + 1) * binWidth)
    val contours = {
      MarchingSquares(levels, grid).zip(levels).flatMap { case (levelPoints, level) =>
        levelPoints.map { path =>
          val color = colorBar match {
            case SingletonColorBar(c) => c
            case colors: ScaledColorBar => colors.getColor(level)
          }
          StrokeStyle(Path(path.map(p => affine(p)), 2), color)
        }
      }
    }.group
    val priors = makePaths(data.priors, affine)
    val best = data.best.map(b => Disc(3, affine(b)) filled HTMLNamedColors.red).getOrElse(EmptyDrawable())
    priors.group behind contours behind best
  }

  private def makePaths(pathPoints: Seq[Seq[Point]],
                        affine: AffineTransform): Seq[Drawable] = {
    pathPoints.map { points =>
      if (points.take(1).nonEmpty && points.tail.isEmpty)
        Disc(3, affine(points.head)) filled HEX("#008000")
      else Path(points.map(affine.apply) :+ affine(points.head), 2) colored HEX("#008000")
    }
  }
}
