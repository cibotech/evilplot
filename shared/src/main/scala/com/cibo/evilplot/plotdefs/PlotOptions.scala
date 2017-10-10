/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.plotdefs

import com.cibo.evilplot.colors.{Color, DefaultColors, HTMLNamedColors}
import com.cibo.evilplot.numeric.Bounds

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
                       gridColor: Color = HTMLNamedColors.white,
                       withinMetrics: Option[Seq[Double]] = None,
                       backgroundColor: Color = DefaultColors.backgroundColor,
                       barColor: Color = DefaultColors.barColor)

