package com.cibo.evilplot.plot.renderers

import com.cibo.evilplot.colors.{Color, DefaultColors}
import com.cibo.evilplot.geometry.{Drawable, Path, StrokeStyle}
import com.cibo.evilplot.numeric.{GridData, MarchingSquares, Point, Point3}

trait SurfaceRenderer {
  def render(points: Seq[Point3]): Drawable
}

object SurfaceRenderer {
  private val defaultStrokeWidth: Double = 2
  def contours(strokeWidth: Double = defaultStrokeWidth,
               color: Color = DefaultColors.pathColor): SurfaceRenderer = new SurfaceRenderer {
    def render(points: Seq[Point3]): Drawable = {
      points.grouped(2)
        .map(seg => StrokeStyle(Path(seg.map(p => Point(p.x, p.y)), strokeWidth), color))
        .toSeq.group
    }
  }
}
