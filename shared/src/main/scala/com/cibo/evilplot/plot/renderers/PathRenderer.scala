package com.cibo.evilplot.plot.renderers

import com.cibo.evilplot.colors.{Color, DefaultColors}
import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent, Line, Path, StrokeStyle, Text}
import com.cibo.evilplot.numeric.Point
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.{LegendContext, Plot}

trait PathRenderer extends PlotElementRenderer[Seq[Point]] {
  def legendContext: LegendContext = LegendContext.empty
  def render(plot: Plot, extent: Extent, path: Seq[Point])(implicit theme: Theme): Drawable
}

object PathRenderer {
  val defaultStrokeWidth: Double = 2.0
  private val legendStrokeLength: Double = 8.0

  /** The default path renderer. */
  def default()(implicit theme: Theme): PathRenderer =
    default(defaultStrokeWidth, theme.colors.path, EmptyDrawable())

  /** The default path renderer.
    * @param strokeWidth The width of the path.
    * @param color Point color.
    * @param label A label for this path (for legends).
    */
  def default(
    strokeWidth: Double,
    color: Color,
    label: Drawable
  )(implicit theme: Theme): PathRenderer = new PathRenderer {
    override def legendContext: LegendContext = label match {
      case _: EmptyDrawable => LegendContext.empty
      case d                => LegendContext.single(StrokeStyle(Line(legendStrokeLength, strokeWidth), color), d)
    }
    def render(plot: Plot, extent: Extent, path: Seq[Point])(implicit theme: Theme): Drawable = {
      StrokeStyle(Path(path, strokeWidth), color)
    }
  }

  /** Path renderer for named paths (to be shown in legends).
    * @param name The name of this path.
    * @param color The color of this path.
    * @param strokeWidth The width of the path.
    */
  def named(
    name: String,
    color: Color,
    strokeWidth: Double = defaultStrokeWidth
  )(implicit theme: Theme): PathRenderer = default(strokeWidth, color, Text(name))

  def closed(strokeWidth: Double = defaultStrokeWidth,
             color: Color = DefaultColors.pathColor
            ): PathRenderer = new PathRenderer {
    def render(plot: Plot, extent: Extent, path: Seq[Point])(implicit theme: Theme): Drawable = {
      // better hope this is an indexedseq?
      path.headOption match {
        case Some(h) => StrokeStyle(Path(path :+ h, strokeWidth), color)
        case None    => EmptyDrawable()
      }
    }
  }

  /**
    * A no-op renderer for when you don't want to render paths (such as on a scatter plot)
    */
  def empty[T](): PathRenderer = new PathRenderer {
    def render(plot: Plot, extent: Extent, path: Seq[Point])(implicit theme: Theme): Drawable =
      EmptyDrawable()
  }
}

