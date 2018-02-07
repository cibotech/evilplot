package com.cibo.evilplot

import com.cibo.evilplot.plot.components._

package object plot {

  implicit class RichPlot[T](
    protected val plot: Plot[T]
  ) extends Axes.AxesImplicits[T]
    with PlotLineImplicits[T]
    with AnnotationImplicits[T]
    with BackgroundImplicits[T]
    with LabelImplicits[T]
    with PadImplicits[T]

}
