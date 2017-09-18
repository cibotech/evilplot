/*
 * Copyright 2017 CiBO Technologies
 */

package com.cibo.evilplot.plotdefs

import com.cibo.evilplot.colors.{Color, HSL}
import com.cibo.evilplot.geometry.Extent
import com.cibo.evilplot.numeric.{Bounds, GridData, Point}

sealed trait PlotDef {
  val extent: Extent
  val options: PlotOptions
}
case class FacetsDef(extent: Extent, numRows: Int, numCols: Int, defs: Seq[PlotDef], columnLabels: Option[Seq[String]],
                     rowLabels: Option[Seq[String]], axisScales: ScaleOption, options: PlotOptions) extends PlotDef
case class ScatterPlotDef(extent: Extent, data: Seq[Point], color: HSL,
                          options: PlotOptions = PlotOptions()) extends PlotDef
case class ContourPlotDef(extent: Extent, data: GridData, numContours: Int,
                          options: PlotOptions = PlotOptions()) extends PlotDef
case class HistogramPlotDef(extent: Extent, data: Seq[Double], numBins: Int,
                            options: PlotOptions = PlotOptions()) extends PlotDef

// boxplot takes a Seq[T], not readily serializable using auto derivation. just calling it a string for now.
case class BoxPlotDef(extent: Extent, labels: Seq[String], distributions: Seq[Seq[Double]],
                      drawPoints: BoxPlotPoints = AllPoints, rectWidth: Option[Double] = None,
                      rectSpacing: Option[Double] = None, rectColor: HSL = HSL(235, 100, 50),
                      pointColor: HSL = HSL(0, 0, 0), pointSize: Double = 2.0,
                      options: PlotOptions = PlotOptions()) extends PlotDef

// this should use OneLinePlotData, but we need to get colors working, doesn't make sense to destroy things over there
//  case class LinePlotDef(extent: Extent, lines: Seq[Seq[Point]], colors: Seq[HSL],
//                         options: PlotOptions = PlotOptions()) extends PlotDef
// ignore pie chart for now, is anyone going to use it?
// I don't like the name counts for this, not necessarily integral.
case class BarChartDef(extent: Extent, counts: Seq[Double], labels: Seq[String], barWidth: Option[Double] = None,
                       barSpacing: Option[Double] = None, options: PlotOptions = PlotOptions()) extends PlotDef

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
// Plot associated enums?
sealed trait ScaleOption
case object FixedScales extends ScaleOption
case object FreeScales extends ScaleOption

sealed trait BoxPlotPoints
case object AllPoints extends BoxPlotPoints
case object OutliersOnly extends BoxPlotPoints
case object NoPoints extends BoxPlotPoints

// TODO: Split generic parts of the configuration out.
case class PlotOptions(title: Option[String] = None,
                       xAxisBounds: Option[Bounds] = None,
                       yAxisBounds: Option[Bounds] = None,
                       drawXAxis: Boolean = true,
                       drawYAxis: Boolean = true,
                       numXTicks: Option[Int] = None,
                       numYTicks: Option[Int] = None,
                       xAxisLabel: Option[String] = None,
                       yAxisLabel: Option[String] = None,
                       topLabel: Option[String] = None,
                       rightLabel: Option[String] = None,
                       xGridSpacing: Option[Double] = None,
                       yGridSpacing: Option[Double] = None,
                       gridColor: Color = HSL(0, 0, 0),
                       withinMetrics: Option[Seq[Double]] = None,
                       backgroundColor: HSL = HSL(0, 0, 92),
                       barColor: HSL = HSL(0, 0, 35))

