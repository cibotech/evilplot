package com.cibo.evilplot.plot

import com.cibo.evilplot.StrokeStyle
import com.cibo.evilplot.colors.Colors.{ColorSeq, ScaledColorBar}
import com.cibo.evilplot.geometry.{Drawable, Extent, Path}
import com.cibo.evilplot.numeric._
import com.cibo.evilplot.plotdefs.{ContourPlotDef, PlotOptions}

class ContourPlot(val chartSize: Extent, data: ContourPlotDef)
  extends Chart with ContinuousAxes {
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
    val colors = ColorSeq.getGradientSeq(numContours)
    val colorBar = ScaledColorBar(colors, grid.zBounds.min, grid.zBounds.max)
    val binWidth = data.zBounds.range / numContours
    val levels = Seq.tabulate[Double](numContours - 1)(bin => grid.zBounds.min + (bin + 1) * binWidth)
    val contours = for { z <- levels
                         contourSegments = MarchingSquares.getContoursAt(z, grid)
                         if contourSegments.nonEmpty
    } yield contourSegments.map { seg => StrokeStyle(colorBar.getColor(z))(
      Path(toPixelCoords(seg, xAxisDescriptor.axisBounds, yAxisDescriptor.axisBounds, extent), 2)) }

    contours.flatten.group
  }


}

