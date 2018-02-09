package com.cibo.evilplot.plot.components

import com.cibo.evilplot.geometry._
import com.cibo.evilplot.plot.{LegendContext, Plot}

case class Legend(
  position: Position,
  context: LegendContext[_],
  x: Double,
  y: Double
) extends PlotComponent {

  val leftPadding: Double = 4
  val spacing: Double = 4

  private lazy val labels: Seq[Drawable] = context.labels
  private lazy val elementSize: Double = labels.maxBy(_.extent.height).extent.height

  override def size[T](plot: Plot[T]): Extent = {
    val height = context.categories.size * (elementSize + spacing)
    val width = elementSize + labels.maxBy(_.extent.width).extent.width + leftPadding + spacing
    Extent(width, height)
  }

  private def renderContext[C](ctx: LegendContext[C]): Drawable = {
    val elementExtent = Extent(elementSize, elementSize)
    ctx.categories.zip(labels).map { case (category, label) =>
      // The indicator will render itself centered on the origin, so we need to translate.
      val indicator = Resize(
        ctx.renderer.render(elementExtent, category).translate(
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

  def render[T](plot: Plot[T], extent: Extent): Drawable = {
    if (context.categories.nonEmpty) {
      val rendered = renderContext(context)
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

  private def setLegend(position: Position, x: Double = 0, y: Double = 0): Plot[T] = plot.legendContext match {
    case Some(context) => plot :+ Legend(position, context, x, y)
    case None          => plot
  }

  /** Place a legend on the right side of the plot. */
  def rightLegend: Plot[T] = setLegend(Position.Right)

  /** Place a legend on the left side of the plot. */
  def leftLegend: Plot[T] = setLegend(Position.Left)

  /** Overlay a legend on the plot.
    * @param x The relative X position (0 to 1).
    * @param y The relative y position (0 to 1).
    */
  def overlayLegend(x: Double = 1.0, y: Double = 0.0): Plot[T] = {
    setLegend(Position.Overlay, x, y)
  }

  /** Get the legend as a drawable. */
  def renderLegend: Option[Drawable] = plot.legendContext.map { ctx =>
    val legend = Legend(Position.Right, ctx, 0, 0)
    legend.render(plot, legend.size(plot))
  }
}
