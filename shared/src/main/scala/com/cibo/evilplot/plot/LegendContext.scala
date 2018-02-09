package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.Drawable
import com.cibo.evilplot.plot.renderers.PlotElementRenderer

case class LegendContext[C](
  categories: Seq[C],
  renderer: PlotElementRenderer[C],
  labelFunction: C => Drawable
) {
  def labels: Seq[Drawable] = categories.map(labelFunction)
}
