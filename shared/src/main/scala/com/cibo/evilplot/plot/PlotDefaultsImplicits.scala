package com.cibo.evilplot.plot

trait PlotDefaultsImplicits[T] {
  protected val plot: Plot[T]
  def standard(xLabels: Seq[String] = Seq.empty,
               yLabels: Seq[String] = Seq.empty): Plot[T] = {
    // Use the right xAxis / yAxis overload.
    val withX = if (xLabels.isEmpty) plot.xAxis() else plot.xAxis(xLabels)
    val withXY = if (yLabels.isEmpty) withX.yAxis() else withX.yAxis(yLabels)
    withXY.xGrid().yGrid().background()
  }
}
