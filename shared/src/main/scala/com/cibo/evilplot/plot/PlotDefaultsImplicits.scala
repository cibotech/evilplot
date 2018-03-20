package com.cibo.evilplot.plot

import com.cibo.evilplot.plot.aesthetics.Theme

trait PlotDefaultsImplicits {
  protected val plot: Plot
  def standard(xLabels: Seq[String] = Seq.empty,
               yLabels: Seq[String] = Seq.empty)(implicit theme: Theme): Plot = {
    // Use the right xAxis / yAxis overload.
    val withX = if (xLabels.isEmpty) plot.xAxis() else plot.xAxis(xLabels)
    val withXY = if (yLabels.isEmpty) withX.yAxis() else withX.yAxis(yLabels)
    withXY.xGrid().yGrid().background()
  }
}
