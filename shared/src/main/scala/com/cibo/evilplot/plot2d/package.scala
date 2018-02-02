package com.cibo.evilplot

package object plot2d {

  implicit class RichPlot2D[T](
    protected val plot: Plot2D[T]
  ) extends Axes.AxesImplicits[T] with PlotAnnotation.AnnotationImplicits[T]

}
