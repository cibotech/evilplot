package com.cibo.evilplot.plot

import com.cibo.evilplot.colors.ScaledColorBar
import com.cibo.evilplot.geometry.{Drawable, Rect, Text}

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
  elements: Seq[Drawable] = Seq.empty,
  labels: Seq[Drawable] = Seq.empty,
  defaultStyle: LegendStyle = LegendStyle.Categorical
) {
  require(elements.lengthCompare(labels.size) == 0)

  def isEmpty: Boolean = elements.isEmpty
  def nonEmpty: Boolean = !isEmpty

  // Combine this LegendContext with another, taking only new content.
  def combine(other: LegendContext): LegendContext = {
    val oldElementLabels = elements.zip(labels)
    val newElementLabels = other.elements.zip(other.labels).filterNot(oldElementLabels.contains)
    copy(
      elements = elements ++ newElementLabels.map(_._1),
      labels = labels ++ newElementLabels.map(_._2),
      defaultStyle = if (nonEmpty) defaultStyle else other.defaultStyle
    )
  }
}

object LegendContext {
  def empty: LegendContext = LegendContext()

  def single(
    element: Drawable,
    label: Drawable
  ): LegendContext = LegendContext(Seq(element), Seq(label), LegendStyle.Categorical)

  def single(
    element: Drawable,
    label: String
  ): LegendContext = single(element, Text(label))

  def fromColorBar(
    colorBar: ScaledColorBar,
    style: LegendStyle = LegendStyle.Gradient
  ): LegendContext = {
    val elements = (0 until colorBar.nColors).map { c =>
      Rect(Text.defaultSize, Text.defaultSize).filled(colorBar.getColor(c))
    }
    val labels = (0 until colorBar.nColors).map { c =>
      val value = style match {
        case LegendStyle.Gradient if c == 0 =>
          // Floor if labeling the first value in a gradient.
          math.floor(colorBar.colorValue (c))
        case LegendStyle.Gradient =>
          // Ceiling if labeling the last value in a gradient.
          math.ceil(colorBar.colorValue (c))
        case LegendStyle.Categorical =>
          // Otherwise round
          math.round(colorBar.colorValue(c))
      }
      Text(value.toString)
    }
    LegendContext(
      elements = elements,
      labels = labels,
      defaultStyle = LegendStyle.Gradient
    )
  }
}
