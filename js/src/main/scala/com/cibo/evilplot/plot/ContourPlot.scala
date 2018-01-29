package com.cibo.evilplot.plot

import com.cibo.evilplot.colors.Colors
import com.cibo.evilplot.colors.Colors.{ScaledColorBar, SingletonColorBar}
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

  private def toPixelCoords(seg: Segment, xBounds: Bounds, yBounds: Bounds, extent: Extent): Segment = {
    Segment(toPixelCoords(seg.a, xBounds, yBounds, extent), toPixelCoords(seg.b, xBounds, yBounds, extent))
  }

  def plottedData(extent: Extent): Drawable = {
    val colorBar: Colors.ColorBar = data.colorBar
    val binWidth = data.zBounds.range / numContours
    val levels = Seq.tabulate[Double](numContours - 1)(bin => grid.zBounds.min + (bin + 1) * binWidth)
    val contours = for { z <- levels
                         contourSegments = MarchingSquares.getContoursAt(z, grid)
                         if contourSegments.nonEmpty
    } yield contourSegments.map {
      seg => StrokeStyle { colorBar match {
        case SingletonColorBar(color) => color
        case colors: ScaledColorBar => colors.getColor(z)
      }
      }(Path(toPixelCoords(seg, xAxisDescriptor.axisBounds, yAxisDescriptor.axisBounds, extent), 2)) }

    contours.flatten.group
  }


}

