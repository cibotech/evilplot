package com.cibo.evilplot.plot.renderers

import com.cibo.evilplot.colors.{Color, DefaultColors, ScaledColorBar}
import com.cibo.evilplot.geometry.{Drawable, Extent, Line, Path, Rect, StrokeStyle, Text}
import com.cibo.evilplot.numeric.{Point, Point3}
import com.cibo.evilplot.plot.LegendContext

trait SurfaceRenderer extends PlotElementRenderer[Seq[Seq[Point3]], Seq[Point3]] {
  def render(extent: Extent, data: Seq[Seq[Point3]], surface: Seq[Point3]): Drawable

}

object SurfaceRenderer {

  private val defaultStrokeWidth: Double = 2

  def contours(
    strokeWidth: Double = defaultStrokeWidth,
    color: Color = DefaultColors.pathColor
  ): SurfaceRenderer = new SurfaceRenderer {
    def render(extent: Extent, data: Seq[Seq[Point3]], surface: Seq[Point3]): Drawable = {
      surface.grouped(2).map { seg =>
        StrokeStyle(Path(seg.map(p => Point(p.x, p.y)), strokeWidth), color)
      }.toSeq.group
    }
  }

  def densityColorContours(
    strokeWidth: Double = defaultStrokeWidth
  ): SurfaceRenderer = new SurfaceRenderer {
    override def legendContext(points: Seq[Seq[Point3]]): Option[LegendContext[Seq[Point3]]] = {
      val bar = ScaledColorBar(
        Color.stream.take(points.length),
        points.minBy(_.head.z).head.z,
        points.maxBy(_.head.z).head.z
      )
      Some(
        LegendContext[Seq[Point3]](
          categories = (0 until bar.nColors).map { c => Seq(Point3(0, 0, bar.colorValue(c))) },
          elementFunction = (c: Seq[Point3]) => Rect(1, 1).filled(bar.getColor(c.head.z)),
          labelFunction = (c: Seq[Point3]) => Text(math.round(c.head.z).toString),
          discrete = false
        )
      )
    }
    def render(extent: Extent, data: Seq[Seq[Point3]], surface: Seq[Point3]): Drawable = {
      val bar = ScaledColorBar(
        Color.stream.take(data.length),
        data.minBy(_.head.z).head.z,
        data.maxBy(_.head.z).head.z
      )
      val surfaceRenderer = densityColorContours(strokeWidth, bar)
      surfaceRenderer.render(extent, data, surface)
    }
  }

  def densityColorContours(
    strokeWidth: Double,
    bar: ScaledColorBar
  ): SurfaceRenderer = new SurfaceRenderer {
    def render(extent: Extent, data: Seq[Seq[Point3]], points: Seq[Point3]): Drawable = {
      val levelRenderer = contours(strokeWidth, bar.getColor(points.head.z))
      levelRenderer.render(extent, data, points)
    }
  }
}
