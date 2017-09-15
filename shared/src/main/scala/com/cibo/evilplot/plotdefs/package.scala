package com.cibo.evilplot

import com.cibo.evilplot.colors.HSL
import com.cibo.evilplot.numeric.{Bounds, Point}

package object plotdefs {
  sealed trait PlotDef
  case class ScatterPlotDef(data: Seq[Point], color: HSL) extends PlotDef

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
                         backgroundColor: HSL = HSL(0, 0, 92),
                         barColor: HSL = HSL(0, 0, 35))

}
