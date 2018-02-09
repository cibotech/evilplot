package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.Drawable
import com.cibo.evilplot.plot.renderers.PlotElementRenderer

// TODO: remove T
case class LegendContext[T, C](
  categories: Seq[C],
  elementFunction: C => Drawable,
  labelFunction: C => Drawable,
  discrete: Boolean
) {
  def labels: Seq[Drawable] = categories.map(labelFunction)
}
