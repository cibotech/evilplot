package com.cibo.evilplot.plot.renderers

import com.cibo.evilplot.geometry._
import com.cibo.evilplot.plot.{LegendContext, LegendStyle}

/** Renderer to convert data and a legend context into a drawable.
  * @param reduction A function to combine multiple legends.
  */
abstract class LegendRenderer(val reduction: (Drawable, Drawable) => Drawable) {
  def render[T, C](data: T, context: LegendContext[C]): Drawable
}

object LegendRenderer {

  val leftPadding: Double = 4
  val spacing: Double = 4

  /** Create a legend for discrete components.
    * @param reduction Function to combine multiple legends.
    */
  def discrete(
    reduction: (Drawable, Drawable) => Drawable = above
  ): LegendRenderer = new LegendRenderer(reduction) {
    def render[T, C](data: T, context: LegendContext[C]): Drawable = {
      val labels = context.labels
      val elementSize = labels.maxBy(_.extent.height).extent.height
      val elementExtent = Extent(elementSize, elementSize)
      context.levels.zip(labels).map { case (category, label) =>
        // The indicator will render itself centered on the origin, so we need to translate.
        val indicator = fit(context.elementFunction(category), elementExtent)
        indicator.beside(label.padLeft(leftPadding)).padAll(spacing / 2)
      }.reduce(reduction)
    }
  }

  /** Create a legend with a gradient for continuous components.
    * @param reduction Function to combine multiple legends.
    */
  def gradient(
    reduction: (Drawable, Drawable) => Drawable = above
  ): LegendRenderer = new LegendRenderer(reduction) {
    def render[T, C](data: T, context: LegendContext[C]): Drawable = {
      val (startLabel, stopLabel) = (context.labels.head, context.labels.last)
      val elementSize = math.max(startLabel.extent.height, stopLabel.extent.height)
      val elementExtent = Extent(elementSize, elementSize)
      val inner = context.levels.map { category =>
        fit(context.elementFunction(category), elementExtent)
      }.reduce(reduction)
      Seq(startLabel.padAll(spacing / 2), inner, stopLabel.padAll(spacing / 2)).reduce(reduction)
    }
  }

  /** Create a legend using the default style
    * @param reduction Function to combine multiple legends.
    */
  def default(
    reduction: (Drawable, Drawable) => Drawable = above
  ): LegendRenderer = new LegendRenderer(reduction) {
    def render[T, C](data: T, context: LegendContext[C]): Drawable = {
      context.defaultStyle match {
        case LegendStyle.Categorical => discrete(reduction).render(data, context)
        case LegendStyle.Gradient    => gradient(reduction).render(data, context)
      }
    }
  }

  def vertical(): LegendRenderer = default(above)
  def horizontal(): LegendRenderer = default(beside)

  def verticalDiscrete(): LegendRenderer = discrete(above)
  def horizontalDiscrete(): LegendRenderer = discrete(beside)

  def verticalGradient(): LegendRenderer = gradient(above)
  def horizontalGradient(): LegendRenderer = gradient(beside)
}
