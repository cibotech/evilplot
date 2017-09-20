/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.plotdefs

import com.cibo.evilplot.colors.{Color, HSL, HTMLNamedColors}
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
                       backgroundColor: HSL = HSL(0, 0, 92),
                       barColor: HSL = HSL(0, 0, 35))

