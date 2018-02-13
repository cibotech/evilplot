package com.cibo.evilplot

import com.cibo.evilplot.plot.components._

package object plot {

  implicit class RichPlot(
    protected val plot: Plot
  ) extends Axes.AxesImplicits
    with PlotLineImplicits
    with AnnotationImplicits
    with BackgroundImplicits
    with BorderPlotImplicits
    with LabelImplicits
    with LegendImplicits
    with PadImplicits
    with PlotDefaultsImplicits
    with OverlayImplicits
}
