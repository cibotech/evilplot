package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.Drawable
import com.cibo.evilplot.plot.renderers.PlotElementRenderer

case class LegendContext[C](
  categories: Seq[C],
  elementFunction: C => Drawable,
  labelFunction: C => Drawable,
  discrete: Boolean
) {
  def labels: Seq[Drawable] = categories.map(labelFunction)
}
