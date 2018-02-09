package com.cibo.evilplot.plot.renderers

import com.cibo.evilplot.geometry._
import com.cibo.evilplot.plot.LegendContext

trait LegendRenderer {
  def render[T, C](data: T, context: LegendContext[T, C]): Drawable
}

object LegendRenderer {

  val leftPadding: Double = 4
  val spacing: Double = 4

  def discrete(
    reduction: (Drawable, Drawable) => Drawable = above
  ): LegendRenderer = new LegendRenderer {
    def render[T, C](data: T, context: LegendContext[T, C]): Drawable = {
      val labels = context.labels
      val elementSize = labels.maxBy(_.extent.height).extent.height
      val elementExtent = Extent(elementSize, elementSize)
      context.categories.zip(labels).map { case (category, label) =>
        // The indicator will render itself centered on the origin, so we need to translate.
        val indicator = fit(context.elementFunction(category), elementExtent)
        indicator.beside(label.padLeft(leftPadding)).padAll(spacing / 2)
      }.reduce(reduction)
    }
  }

  def gradient(
    reduction: (Drawable, Drawable) => Drawable = above
  ): LegendRenderer = new LegendRenderer {
    def render[T, C](data: T, context: LegendContext[T, C]): Drawable = {
      val (startLabel, stopLabel) = (context.labels.head, context.labels.last)
      val elementSize = math.max(startLabel.extent.height, stopLabel.extent.height)
      val elementExtent = Extent(elementSize, elementSize)
      val inner = context.categories.map { category =>
        fit(context.elementFunction(category), elementExtent)
      }.reduce(reduction)
      Seq(startLabel.padAll(spacing / 2), inner, stopLabel.padAll(spacing / 2)).reduce(reduction)
    }
  }

  def default(
    reduction: (Drawable, Drawable) => Drawable = above
  ): LegendRenderer = new LegendRenderer {
    def render[T, C](data: T, context: LegendContext[T, C]): Drawable = {
      if (context.discrete) {
        discrete(reduction).render(data, context)
      } else {
        gradient(reduction).render(data, context)
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
