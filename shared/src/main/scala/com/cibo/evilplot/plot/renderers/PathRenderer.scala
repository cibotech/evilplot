package com.cibo.evilplot.plot.renderers

import com.cibo.evilplot.colors.{Color, DefaultColors}
import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent, Path, StrokeStyle}
import com.cibo.evilplot.numeric.Point

abstract class PathRenderer extends PlotElementRenderer[Seq[Point]] {
  def render(extent: Extent, path: Seq[Point]): Drawable
}

object PathRenderer {
  val defaultStrokeWidth: Double = 2.0

  def default(strokeWidth: Double = defaultStrokeWidth,
              color: Color = DefaultColors.pathColor
             ): PathRenderer = new PathRenderer {
    def render(extent: Extent, path: Seq[Point]): Drawable = {
      StrokeStyle(Path(path, strokeWidth), color)
    }
  }

  def closed(strokeWidth: Double = defaultStrokeWidth,
             color: Color = DefaultColors.pathColor
            ): PathRenderer = new PathRenderer {
    def render(extent: Extent, path: Seq[Point]): Drawable = {
      // better hope this is an indexedseq?
      StrokeStyle(Path(path :+ path.head, strokeWidth), color)
    }
  }

  /**
    * A no-op renderer for when you don't want to render paths (such as on a scatter plot)
    */
  def empty(): PathRenderer = new PathRenderer {
    override def render(extent: Extent, path: Seq[Point]): Drawable = new EmptyDrawable
  }


}

