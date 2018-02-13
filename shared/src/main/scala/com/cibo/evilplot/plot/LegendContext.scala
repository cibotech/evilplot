package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.{Drawable, Text}
import com.cibo.evilplot.plot.renderers.PlotElementRenderer

sealed trait LegendStyle

object LegendStyle {
  case object Gradient extends LegendStyle  // A legend of levels represented using a gradient.
  case object Categorical extends LegendStyle  // A legend with distinct categories.
}

/** Context information used to render a legend for a plot.
  * @param levels The categories or levels to display for the legend.
  * @param elementFunction A function to render a drawable to represent a level.
  * @param labelFunction A function to render a drawable to label a level.
  * @param defaultStyle The default legend style to render.
  * @tparam C The type of levels.
  */
case class LegendContext[C](
  levels: Seq[C],
  elementFunction: C => Drawable,
  labelFunction: C => Drawable,
  defaultStyle: LegendStyle
) {
  def labels: Seq[Drawable] = levels.map(labelFunction)
}

object LegendContext {
  def single[C](
    value: C,
    element: Drawable,
    label: Drawable
  ): LegendContext[C] = LegendContext(
    levels = Seq(value),
    elementFunction = (c: C) => element,
    labelFunction = (c: C) => label,
    defaultStyle = LegendStyle.Categorical
  )

  def single[C](
    value: C,
    element: Drawable,
    label: String
  ): LegendContext[C] = single(value, element, Text(label))
}
