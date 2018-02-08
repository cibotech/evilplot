package com.cibo.evilplot.plot.renderers

import com.cibo.evilplot.colors.{Color, DefaultColors, ScaledColorBar}
import com.cibo.evilplot.geometry.{Drawable, Path, StrokeStyle}
import com.cibo.evilplot.numeric.{Point, Point3}

trait SurfaceRenderer {
  def render(points: Seq[Seq[Point3]]): Drawable
}

object SurfaceRenderer {
  private val defaultStrokeWidth: Double = 2
  def contours(strokeWidth: Double = defaultStrokeWidth,
               color: Color = DefaultColors.pathColor): SurfaceRenderer = new SurfaceRenderer {
    def render(points: Seq[Seq[Point3]]): Drawable = {
      points.map(_.grouped(2)
        .map(seg => StrokeStyle(Path(seg.map(p => Point(p.x, p.y)), strokeWidth), color))
        .toSeq.group).group
    }
  }

  def densityColorContours(strokeWidth: Double = defaultStrokeWidth): SurfaceRenderer = new SurfaceRenderer {
    def render(points: Seq[Seq[Point3]]): Drawable = {
      val bar: ScaledColorBar = ScaledColorBar(Color.stream.take(points.length),
        points.minBy(_.head.z).head.z, points.maxBy(_.head.z).head.z)
      densityColorContours(strokeWidth, bar).render(points)
    }
  }

  def densityColorContours(strokeWidth: Double,
                          bar: ScaledColorBar): SurfaceRenderer = new SurfaceRenderer {
    def render(points: Seq[Seq[Point3]]): Drawable = {
      points.map { pointsAtLevel =>
        val levelRenderer = contours(strokeWidth, bar.getColor(pointsAtLevel.head.z))
        levelRenderer.render(Seq(pointsAtLevel))
      }.group
    }
  }
}
