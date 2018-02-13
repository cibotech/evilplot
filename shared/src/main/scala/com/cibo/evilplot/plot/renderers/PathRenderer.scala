package com.cibo.evilplot.plot.renderers

import com.cibo.evilplot.colors.{Color, DefaultColors}
import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent, Line, Path, StrokeStyle, Text}
import com.cibo.evilplot.numeric.Point
import com.cibo.evilplot.plot.LegendContext

trait PathRenderer extends PlotElementRenderer[Seq[Point], Seq[Point]] {
  def render(extent: Extent, data: Seq[Point], path: Seq[Point]): Drawable
}

object PathRenderer {
  val defaultStrokeWidth: Double = 2.0

  /** The default path renderer.
    * @param strokeWidth The width of the path.
    * @param color The color of the path.
    * @param name The name of the path (for legends).
    */
  def default(
    strokeWidth: Double = defaultStrokeWidth,
    color: Color = DefaultColors.pathColor,
    name: Option[String] = None
  ): PathRenderer = new PathRenderer {
    override def legendContext(data: Seq[Point]): Option[LegendContext[Seq[Point]]] = {
      name.map { n =>
        LegendContext.single(data, StrokeStyle(Line(10, strokeWidth), color), n)
      }
    }
    def render(extent: Extent, data: Seq[Point], path: Seq[Point]): Drawable = {
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
  ): PathRenderer = default(strokeWidth, color, Some(name))

  def closed(strokeWidth: Double = defaultStrokeWidth,
             color: Color = DefaultColors.pathColor
            ): PathRenderer = new PathRenderer {
    def render(extent: Extent, data: Seq[Point], path: Seq[Point]): Drawable = {
      // better hope this is an indexedseq?
      StrokeStyle(Path(path :+ path.head, strokeWidth), color)
    }
  }

  /**
    * A no-op renderer for when you don't want to render paths (such as on a scatter plot)
    */
  def empty(): PathRenderer = new PathRenderer {
    override def render(extent: Extent, data: Seq[Point], path: Seq[Point]): Drawable = new EmptyDrawable
  }
}

