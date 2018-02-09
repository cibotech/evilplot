package com.cibo.evilplot.plot.components

import com.cibo.evilplot.geometry._
import com.cibo.evilplot.plot.renderers.LegendRenderer
import com.cibo.evilplot.plot.{LegendContext, Plot}

case class Legend[T](
  position: Position,
  data: T,
  context: LegendContext[T, _],
  legendRenderer: LegendRenderer,
  x: Double,
  y: Double
) extends PlotComponent {

  private def getDrawable: Drawable = legendRenderer.render(data, context)

  override def size[X](plot: Plot[X]): Extent = getDrawable.extent

  def render[X](plot: Plot[X], extent: Extent): Drawable = {
    if (context.categories.nonEmpty) {
      val drawable = getDrawable
      drawable.translate(
        x = (extent.width - drawable.extent.width) * x,
        y = (extent.height - drawable.extent.height) * y
      )
    } else {
      EmptyDrawable()
    }
  }
}

trait LegendImplicits[T] {
  protected val plot: Plot[T]

  private def setLegend(
    position: Position,
    renderer: LegendRenderer,
    x: Double = 0,
    y: Double = 0
  ): Plot[T] = plot.legendContext match {
    case Some(context) => plot :+ Legend(position, plot.data, context, renderer, x, y)
    case None          => plot
  }

  /** Place a legend on the right side of the plot. */
  def rightLegend(
    renderer: LegendRenderer = LegendRenderer.vertical()
  ): Plot[T] = setLegend(Position.Right, renderer)

  /** Place a legend on the left side of the plot. */
  def leftLegend(
    renderer: LegendRenderer = LegendRenderer.vertical()
  ): Plot[T] = setLegend(Position.Left, renderer)

  /** Place a legend on the top of the plot. */
  def topLegend(
    renderer: LegendRenderer = LegendRenderer.horizontal()
  ): Plot[T] = setLegend(Position.Top, renderer)

  /** Place a legend on the bottom of the plot. */
  def bottomLegend(
    renderer: LegendRenderer = LegendRenderer.horizontal()
  ): Plot[T] = setLegend(Position.Bottom, renderer)

  /** Overlay a legend on the plot.
    * @param x The relative X position (0 to 1).
    * @param y The relative y position (0 to 1).
    * @param renderer The legend renderer to use.
    */
  def overlayLegend(
    x: Double = 1.0,
    y: Double = 0.0,
    renderer: LegendRenderer = LegendRenderer.vertical()
  ): Plot[T] = {
    setLegend(Position.Overlay, renderer, x, y)
  }

  /** Get the legend as a drawable. */
  def renderLegend(
    renderer: LegendRenderer = LegendRenderer.vertical()
  ): Option[Drawable] = plot.legendContext.map { ctx =>
    val legend = Legend(Position.Right, plot.data, ctx, renderer, 0, 0)
    legend.render(plot, legend.size(plot))
  }
}
