package com.cibo.evilplot.plot.renderers

import com.cibo.evilplot.geometry._
import com.cibo.evilplot.plot.LegendContext

trait LegendRenderer {
  def render[C](context: LegendContext[C]): Drawable
}

object LegendRenderer {

  val leftPadding: Double = 4
  val spacing: Double = 4

  def vertical(): LegendRenderer = new LegendRenderer {
    def render[C](context: LegendContext[C]): Drawable = {
      val labels = context.labels
      val elementSize = labels.maxBy(_.extent.height).extent.height
      val elementExtent = Extent(elementSize, elementSize)
      context.categories.zip(labels).map { case (category, label) =>
        // The indicator will render itself centered on the origin, so we need to translate.
        val indicator = Resize(
          context.renderer.render(elementExtent, category).translate(
            x = elementSize / 2,
            y = label.extent.height / 2
          ),
          elementExtent
        )
        indicator.beside(
          label.padLeft(leftPadding)
        ).padAll(spacing / 2)
      }.reduce(above)
    }
  }

  def horizontal(): LegendRenderer = new LegendRenderer {
    def render[C](context: LegendContext[C]): Drawable = {
      val labels = context.labels
      val elementSize = labels.maxBy(_.extent.height).extent.height
      val elementExtent = Extent(elementSize, elementSize)
      context.categories.zip(labels).map { case (category, label) =>
        // The indicator will render itself centered on the origin, so we need to translate.
        val indicator = Resize(
          context.renderer.render(elementExtent, category).translate(
            x = elementSize / 2,
            y = label.extent.height / 2
          ),
          elementExtent
        )
        indicator.beside(
          label.padLeft(leftPadding)
        ).padAll(spacing / 2)
      }.reduce(beside)
    }
  }
}
