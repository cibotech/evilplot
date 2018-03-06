package com.cibo.evilplot.plot.aesthetics

import com.cibo.evilplot.colors.Color

trait Colors {
  val background: Color
  val bar: Color
  val fill: Color
  val path: Color
  val gridLine: Color
  val trendLine: Color

  val title: Color
  val label: Color
  val annotation: Color
  val legendLabel: Color
  val tickLabel: Color
}
