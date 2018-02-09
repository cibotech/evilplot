package com.cibo.evilplot.plot.components

import com.cibo.evilplot.geometry._
import com.cibo.evilplot.plot.renderers.LegendRenderer
import com.cibo.evilplot.plot.{LegendContext, Plot}

case class Legend(
  position: Position,
  context: LegendContext[_],
  legendRenderer: LegendRenderer,
  x: Double,
  y: Double
) extends PlotComponent {

  private lazy val rendered: Drawable = legendRenderer.render(context)

  override def size[T](plot: Plot[T]): Extent = rendered.extent

  def render[T](plot: Plot[T], extent: Extent): Drawable = {
    if (context.categories.nonEmpty) {
      rendered.translate(
        x = (extent.width - rendered.extent.width) * x,
        y = (extent.height - rendered.extent.height) * y
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
    case Some(context) => plot :+ Legend(position, context, renderer, x, y)
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
    val legend = Legend(Position.Right, ctx, renderer, 0, 0)
    legend.render(plot, legend.size(plot))
  }
}
