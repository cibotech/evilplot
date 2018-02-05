package com.cibo.evilplot

package object plot {

  implicit class RichPlot[T](
    protected val plot: Plot[T]
  ) extends Axes.AxesImplicits[T] with Label.LabelImplicits[T] with PlotComponent.AnnotationImplicits[T]

}
