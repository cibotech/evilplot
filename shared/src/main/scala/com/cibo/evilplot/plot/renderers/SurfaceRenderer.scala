package com.cibo.evilplot.plot.renderers

import com.cibo.evilplot.colors.{Color, DefaultColors, ScaledColorBar}
import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent, Path, Rect, StrokeStyle, Text}
import com.cibo.evilplot.numeric.{Bounds, Point, Point3}
import com.cibo.evilplot.plot.{LegendContext, LegendStyle}

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
    private def getColorSeq(numPoints: Int): Seq[Color] =
      if (numPoints <= DefaultColors.nicePalette.length) DefaultColors.nicePalette.take(numPoints)
      else Color.stream.take(numPoints)

    def getBySafe[T](data: Seq[T])(f: T => Option[Double]): Option[Bounds] = {
      val mapped = data.map(f).filterNot(_.forall(_.isNaN)).flatten
      Bounds.get(mapped)
    }

    override def legendContext(points: Seq[Seq[Point3]]): Option[LegendContext[Seq[Point3]]] = {
      val colors = getColorSeq(points.length)

      getBySafe(points)(_.headOption.map(_.z)).map { bs =>
        val bar = ScaledColorBar(colors, bs.min, bs.max)
        LegendContext[Seq[Point3]](
          levels = (0 until bar.nColors).map { c => Seq(Point3(0, 0, bar.colorValue(c))) },
          elementFunction = (c: Seq[Point3]) => Rect(1, 1).filled(bar.getColor(c.head.z)),
          labelFunction = (c: Seq[Point3]) => Text(math.round(c.head.z).toString),
          defaultStyle = LegendStyle.Gradient
        )
      }
    }

    def render(extent: Extent, data: Seq[Seq[Point3]], surface: Seq[Point3]): Drawable = {
      val surfaceRenderer = getBySafe(data)(_.headOption.map(_.z)).map { bs =>
        val bar = ScaledColorBar(getColorSeq(data.length), bs.min, bs.max)
        densityColorContours(strokeWidth, bar)
      }.getOrElse(contours(strokeWidth))
      surfaceRenderer.render(extent, data, surface)
    }
  }

  def densityColorContours(
    strokeWidth: Double,
    bar: ScaledColorBar
  ): SurfaceRenderer = new SurfaceRenderer {
    def render(extent: Extent, data: Seq[Seq[Point3]], points: Seq[Point3]): Drawable = {
      points.headOption.map(p => contours(strokeWidth, bar.getColor(p.z))
        .render(extent, data, points)
      )
      .getOrElse(EmptyDrawable())
    }
  }
}
