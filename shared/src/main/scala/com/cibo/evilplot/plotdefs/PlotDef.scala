/*
 * Copyright 2017 CiBO Technologies
 */

package com.cibo.evilplot.plotdefs

import com.cibo.evilplot.colors.Colors.{ColorBar, SingletonColorBar}
import com.cibo.evilplot.colors.{Color, HSL, HTMLNamedColors}
import com.cibo.evilplot.geometry.Extent
import com.cibo.evilplot.numeric._

// A plot definition is a descriptor containing all of the data and settings required for the renderer to construct
// a renderable plot object.

sealed trait PlotDef {
  val extent: Option[Extent] = None // if not supplied, can be passed to the renderer later.
  val options: PlotOptions = PlotOptions()
  def xBounds: Option[Bounds] = None
  def yBounds: Option[Bounds] = None

  def withBounds: PlotDef = this // Lol.

  // Lots of unfortunate boilerplate here. From https://groups.google.com/forum/#!topic/scala-internals/O1yrB1xetUA ,
  // seems like this is moderately unavoidable
  def withOptions(opts: PlotOptions): PlotDef = this match {
    case sp: ScatterPlotDef => sp.copy(options = opts)
    case cp: ContourPlotDef => cp.copy(options = opts)
    case bc: BarChartDef => bc.copy(options = opts)
    case bp: BoxPlotDef => bp.copy(options = opts)
    case lp: LinePlotDef => lp.copy(options = opts)
    case h: HistogramChartDef => h.copy(options = opts)
    case fd: FacetsDef => fd.copy(options = opts) // This change wouldn't actually be registered, would it?
  }
}

final case class ScatterPlotDef(
    data: Seq[Point],
    zData: Option[Seq[Double]] = None,
    pointSize: Double = 2.25,
    colorBar: ColorBar = SingletonColorBar(HTMLNamedColors.black),
    override val extent: Option[Extent] = None,
    override val options: PlotOptions = PlotOptions())
    extends PlotDef {
  override def xBounds: Option[Bounds] =
    Some(Bounds(data.minBy(_.x).x, data.maxBy(_.x).x))
  override def yBounds: Option[Bounds] =
    Some(Bounds(data.minBy(_.y).y, data.maxBy(_.y).y))
}

final case class ContourPlotDef(
    gridData: GridData,
    numContours: Int,
    colorBar: ColorBar = SingletonColorBar(HTMLNamedColors.blue),
    override val extent: Option[Extent] = None,
    override val options: PlotOptions = PlotOptions())
    extends PlotDef {
  override def xBounds: Option[Bounds] = Some(gridData.xBounds)
  override def yBounds: Option[Bounds] = Some(gridData.yBounds)
  def zBounds: Bounds = gridData.zBounds
}

final case class HistogramChartDef(data: Histogram,
                                   annotation: Seq[String] = Nil,
                                   bounds: Option[Bounds] = None,
                                   override val extent: Option[Extent] = None,
                                   override val options: PlotOptions =
                                     PlotOptions())
    extends PlotDef {
  override def xBounds: Option[Bounds] = Some(Bounds(data.min, data.max))

  // Get a new HistogramChartDef, rebinning over `bounds`
  def withBounds(bounds: Bounds): HistogramChartDef = this.copy(
    data = Histogram(data.rawData, data.numBins, Some(bounds)),
    options = options.copy(xAxisBounds = Some(bounds)))
}

final case class BarChartDef(counts: Seq[Double],
                             labels: Seq[String],
                             barWidth: Option[Double] = None,
                             barSpacing: Option[Double] = None,
                             override val extent: Option[Extent] = None,
                             override val options: PlotOptions = PlotOptions())
    extends PlotDef {
  val length: Int = counts.length
  require(counts.length == labels.length,
          "must be same number of data points as labels")
  override def yBounds: Option[Bounds] =
    Some(Bounds(if (counts.min > 0) 0 else counts.min, counts.max))
}

//  reports often plot all the points in the distribution, so there should be
// an option to send. however, by default it shouldn't? Current behavior is serialize it all.
final case class BoxPlotDef(labels: Seq[String],
                            summaries: Seq[BoxPlotSummaryStatistics],
                            drawPoints: BoxPlotPoints = OutliersOnly,
                            rectWidth: Option[Double] = None,
                            rectSpacing: Option[Double] = None,
                            rectColor: Color = HTMLNamedColors.blue,
                            pointColor: Color = HTMLNamedColors.black,
                            pointSize: Double = 2.0,
                            override val extent: Option[Extent] = None,
                            override val options: PlotOptions = PlotOptions())
    extends PlotDef {
  require(labels.length == summaries.length)
  val numBoxes: Int = labels.length
  override def yBounds: Option[Bounds] =
    Some(Bounds(summaries.map(_.min).min, summaries.map(_.max).max))
}

final case class LinePlotDef(lines: Seq[OneLinePlotData],
                             override val extent: Option[Extent] = None,
                             override val options: PlotOptions = PlotOptions())
    extends PlotDef {
  override def xBounds: Option[Bounds] = {
    val bounds = lines.map(_.xBounds)
    val xMin = bounds.map(_.min).min
    val xMax = bounds.map(_.max).max
    Some(Bounds(xMin, xMax))
  }

  override def yBounds: Option[Bounds] = {
    val bounds = lines.map(_.yBounds)
    val yMin = bounds.map(_.min).min
    val yMax = bounds.map(_.max).max
    Some(Bounds(yMin, yMax))
  }
}

/** Directly instantiate a `FacetsDef` from the primary constructor to get the literal plot configuration specified
  * in the `plotDefs` argument. */
final case class FacetsDef(numRows: Int,
                           numCols: Int,
                           plotDefs: Seq[PlotDef],
                           columnLabels: Option[Seq[String]],
                           rowLabels: Option[Seq[String]],
                           override val extent: Option[Extent],
                           override val options: PlotOptions)
    extends PlotDef

/** `apply` methods defined here can "fix" axes and change which facets the labels appear on. */
object FacetsDef {
  /** Supply a generic data object and accessor functions to index into that object corresponding to the columns/rows
    * of the resulting faceted plot. */
  def apply[T, U](dataObject: T,
                  columns: Seq[T => U],
                  rows: Seq[U => PlotDef],
                  columnLabels: Option[Seq[String]] = None,
                  rowLabels: Option[Seq[String]] = None,
                  axisScales: ScaleOption = FixedScales,
                  extent: Option[Extent] = None,
                  baseOptions: PlotOptions = PlotOptions()): FacetsDef = {
    val nRows = rows.length
    val nCols = columns.length
    val naivePlotDefs: Seq[PlotDef] = for (row <- rows; col <- columns) yield row(col(dataObject))
    this.apply(nRows, nCols, naivePlotDefs, columnLabels, rowLabels, axisScales, extent, baseOptions)
  }

  /** "Straightforward": Supply a list of PlotDefs and base options, then adjust based on the configuration options
    * set. */
  def apply(nRows: Int, nCols: Int, plotDefs: Seq[PlotDef], columnLabels: Option[Seq[String]],
            rowLabels: Option[Seq[String]], axisScales: ScaleOption, extent: Option[Extent],
            baseOptions: PlotOptions): FacetsDef = {
    import FacetsDefFunctions._
    // Build the function required to take the "naive" plot definitions to what goes in the finished plot.
    val transformPlotDefs = axisScales match {
      case FixedScales => fixBounds(xAxis)_ compose fixBounds(yAxis) compose bottomXLabels(nRows, nCols) compose
        leftYLabels(nRows, nCols)
      case FixedX => fixBounds(xAxis)_ compose bottomXLabels(nRows, nCols)
      case FixedY => fixBounds(yAxis)_ compose leftYLabels(nRows, nCols)
      case FreeScales => identity[Seq[PlotDef]]_
    }

   FacetsDef(nRows, nCols, transformPlotDefs(plotDefs), columnLabels, rowLabels, extent, baseOptions)
  }


}

// This should probably go in another file?
final case class OneLinePlotData(points: Seq[Point], color: Color) {
  def xBounds: Bounds = {
    val xS = points.map(_.x)
    val xMin = xS.min
    val xMax = xS.max
    Bounds(xMin, xMax)
  }

  def yBounds: Bounds = {
    val yS = points.map(_.y)
    val yMin = yS.min
    val yMax = yS.max
    Bounds(yMin, yMax)
  }
}

/* Faceting related, temporarily disabled.
  case class FacetsDef(extent: Extent, numRows: Int, numCols: Int, defs: Seq[PlotDef], columnLabels: Option[Seq[String]],
                       rowLabels: Option[Seq[String]], axisScales: ScaleOption, options: PlotOptions) extends PlotDef

  object FacetsDef {
    def apply[T, U, V <: PlotDef](extent: Extent,
                                  dataObject: T,
                                  columns: Seq[(T => U)],
                                  rows: Seq[(U => V)],
                                  columnLabels: Option[Seq[String]] = None,
                                  rowLabels: Option[Seq[String]] = None,
                                  axisScales: ScaleOption = FixedScales,
                                  baseOptions: PlotOptions = PlotOptions()): FacetsDef = {
      val defs = for (row <- rows; col <- columns) yield row(col(dataObject))
      FacetsDef(extent, rows.length, columns.length, defs, columnLabels, rowLabels, axisScales, baseOptions)
    }
  }
  sealed trait ScaleOption
  case object FixedScales extends ScaleOption
  case object FreeScales extends ScaleOption*/

sealed trait BoxPlotPoints
case object AllPoints extends BoxPlotPoints
case object OutliersOnly extends BoxPlotPoints
case object NoPoints extends BoxPlotPoints

sealed trait ScaleOption
case object FixedScales extends ScaleOption
case object FixedX extends ScaleOption
case object FixedY extends ScaleOption
case object FreeScales extends ScaleOption
