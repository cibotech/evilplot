package com.cibo.evilplot.oldplot

import com.cibo.evilplot.colors.{ScaledColorBar, SingletonColorBar}
import com.cibo.evilplot.geometry.{Drawable, Extent, Path, StrokeStyle}
import com.cibo.evilplot.numeric._
import com.cibo.evilplot.plotdefs.{ContourPlotDef, PlotOptions}

case class ContourPlot(chartSize: Extent, data: ContourPlotDef) extends Chart with ContinuousAxes {
  val options: PlotOptions = data.options
  private val grid = data.gridData
  private val numContours = data.numContours
  val defaultXAxisBounds: Bounds = data.xBounds.get
  val defaultYAxisBounds: Bounds = data.yBounds.get

  private def toPixelCoords(p: Point, xBounds: Bounds, yBounds: Bounds, extent: Extent): Point = {
    Point((p.x - xBounds.min) * extent.width / xBounds.range, (yBounds.max - p.y) * extent.height / yBounds.range)
  }

  def plottedData(extent: Extent): Drawable = {
    val colorBar = data.colorBar
    val binWidth = data.zBounds.range / numContours
    val levels = Seq.tabulate[Double](numContours - 1)(bin => grid.zBounds.min + (bin + 1) * binWidth)
    val contours = MarchingSquares(levels, grid).zip(levels).flatMap { case (levelPoints, level) =>
      levelPoints.map { path =>
        val color = colorBar match {
          case SingletonColorBar(c) => c
          case colors: ScaledColorBar => colors.getColor(level)
        }
        StrokeStyle(
          Path(path.map(p => toPixelCoords(p, xAxisDescriptor.axisBounds, yAxisDescriptor.axisBounds, extent)), 2),
          color)
      }
    }
    contours.group
  }
}

