package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.{Drawable, Text}
import com.cibo.evilplot.plot.renderers.PlotElementRenderer

sealed trait LegendStyle

object LegendStyle {
  case object Gradient extends LegendStyle  // A legend of levels represented using a gradient.
  case object Categorical extends LegendStyle  // A legend with distinct categories.
}

/** Context information used to render a legend for a plot.
  * @param elements The elements for each level.
  * @param labels Labels for each element.
  * @param defaultStyle The default legend style to render.
  */
case class LegendContext(
  elements: Seq[Drawable],
  labels: Seq[Drawable],
  defaultStyle: LegendStyle
) {
  require(elements.lengthCompare(labels.size) == 0)
}

object LegendContext {
  def single(
    element: Drawable,
    label: Drawable
  ): LegendContext = LegendContext(Seq(element), Seq(label), LegendStyle.Categorical)

  def single(
    element: Drawable,
    label: String
  ): LegendContext = single(element, Text(label))
}
