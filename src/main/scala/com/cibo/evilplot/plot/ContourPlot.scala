package com.cibo.evilplot.plot

import com.cibo.evilplot.colors.Colors.{ColorSeq, ScaledColorBar}
import com.cibo.evilplot.geometry.{Drawable, DrawableLater, DrawableLaterMaker, Extent, Path, Rect}
import com.cibo.evilplot.layout.ChartLayout
import com.cibo.evilplot.numeric.MarchingSquares.Grid
import com.cibo.evilplot.numeric.{AxisDescriptor, MarchingSquares}
import com.cibo.evilplot.{StrokeStyle, Style}
import org.scalajs.dom.raw.CanvasRenderingContext2D

import scala.math.min

case class Point3(x: Double, y: Double, z: Double)
case class GridData(grid: Grid, xBounds: Bounds, yBounds: Bounds, zBounds: Bounds,
                    xSpacing: Double, ySpacing: Double) {
}
object GridData {
  // make a grid data object out of a raw seq of points, assuming those points already lie in a grid.
  // the caller is presumed to know these parameters if they know that their data is already on a grid
  def apply(data: Seq[Point3], xSpacing: Double, ySpacing: Double, xBounds: Bounds, yBounds: Bounds): GridData = {
    val zBounds = Bounds(data.minBy(_.z).z, data.maxBy(_.z).z)
    val numCols: Int = (xBounds.range / xSpacing).toInt
    val numRows: Int = (yBounds.range / ySpacing).toInt
    val _grid: Array[Array[Double]] = Array.fill(numRows)(Array.fill(numCols){0})
    for (Point3(x, y, z) <- data) {
      val row: Int = min(((y - yBounds.min) / ySpacing).toInt, numRows - 1)
      val col: Int = min(((x - xBounds.min) / xSpacing).toInt, numCols - 1)
      _grid(row)(col) = z
    }
    val grid: Grid = _grid.map(_.toVector).toVector
    GridData(grid, xBounds, yBounds, zBounds, xSpacing, ySpacing)
  }
}
case class ContourData(gridData: GridData, numContours: Int) extends PlotData {
  override def xBounds: Option[Bounds] = Some(gridData.xBounds)
  override def yBounds: Option[Bounds] = Some(gridData.yBounds)
  def zBounds: Bounds = gridData.zBounds
  override def createPlot(extent: Extent, options: PlotOptions): Drawable = new ContourPlot(extent, this, options)
}

class ContourPlot(val extent: Extent, data: ContourData, options: PlotOptions) extends Drawable {
  private val grid = data.gridData
  private val numContours = data.numContours
  private val xAxisDescriptor = AxisDescriptor(options.xAxisBounds.getOrElse(grid.xBounds), 10)
  private val yAxisDescriptor = AxisDescriptor(options.yAxisBounds.getOrElse(grid.yBounds), 10)


  private val _drawable: Drawable = {
    val xAxis: DrawableLater = ContinuousChartDistributable.XAxis(xAxisDescriptor, label = options.xAxisLabel)
    val yAxis: DrawableLater = ContinuousChartDistributable.YAxis(yAxisDescriptor, label = options.yAxisLabel)

    def chartArea(extent: Extent): Drawable = {
      val xSpacing = options.xGridSpacing.getOrElse(0.0)
      val ySpacing = options.yGridSpacing.getOrElse(0.0)
      val chartBackground = Style(options.backgroundColor)(Rect(extent))
      val xGridLines = ContinuousChartDistributable.VerticalGridLines(xAxisDescriptor, xSpacing)(extent)
      val yGridLines = ContinuousChartDistributable.HorizontalGridLines(yAxisDescriptor, ySpacing)(extent)
      val _chartArea = chartBackground behind yGridLines behind xGridLines
      val colors = ColorSeq.getGradientSeq(numContours)
      val colorBar = ScaledColorBar(colors, grid.zBounds.min, grid.zBounds.max)
      val binWidth = data.zBounds.range / numContours
      val levels = Seq.tabulate[Double](numContours - 1)(bin => grid.zBounds.min + (bin + 1) * binWidth)
      val contours = for { z <- levels
                          contourSegments = MarchingSquares.getContoursAt(z, grid)
                          if contourSegments.nonEmpty
                         } yield contourSegments.map { seg => StrokeStyle(colorBar.getColor(z))(
                         Path(seg.toPixelCoords(xAxisDescriptor.axisBounds, yAxisDescriptor.axisBounds, extent), 2)) }

      _chartArea behind contours.flatten.group
    }

    new ChartLayout(extent, preferredSizeOfCenter = extent * 0.9, center = new DrawableLaterMaker(chartArea),
      bottom = xAxis, left = yAxis)
  }

  override def draw(canvas: CanvasRenderingContext2D): Unit = _drawable.draw(canvas)
}

