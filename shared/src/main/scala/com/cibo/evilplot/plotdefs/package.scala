package com.cibo.evilplot

import com.cibo.evilplot.colors.{Color, HSL}
import com.cibo.evilplot.numeric.{Bounds, GridData, Point}

package object plotdefs {
  sealed trait PlotDef
  case class ScatterPlotDef(data: Seq[Point], color: Color) extends PlotDef
  case class ContourPlotDef(data: GridData, numContours: Int) extends PlotDef

  sealed trait ScaleOption
  case object FixedScales extends ScaleOption
  case object FreeScales extends ScaleOption

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
                         gridColor: HSL = HSL(0, 0, 0),
                         withinMetrics: Option[Seq[Double]] = None,
                         backgroundColor: Color = HSL(0, 0, 92),
                         barColor: Color = HSL(0, 0, 35))

}
