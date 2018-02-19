package com.cibo.evilplot.plot.renderers

import com.cibo.evilplot.colors.{Color, DefaultColors, ScaledColorBar}
import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent, Path, Rect, StrokeStyle, Text}
import com.cibo.evilplot.numeric.{Bounds, Point, Point3}
import com.cibo.evilplot.plot.{LegendContext, LegendStyle, Plot}

trait SurfaceRenderer extends PlotElementRenderer[Seq[Point3]] {
  def render(plot: Plot, extent: Extent, surface: Seq[Point3]): Drawable
}

object SurfaceRenderer {

  private val defaultStrokeWidth: Double = 2

  def contours(
    strokeWidth: Double = defaultStrokeWidth,
    color: Color = DefaultColors.pathColor
  ): SurfaceRenderer = new SurfaceRenderer {
    def render(plot: Plot, extent: Extent, surface: Seq[Point3]): Drawable = {
      surface.grouped(2).map { seg =>
        Path(seg.map(p => Point(p.x, p.y)), strokeWidth)
      }.toSeq.group colored color
    }
  }

  def densityColorContours(
    strokeWidth: Double = defaultStrokeWidth
  )(points: Seq[Seq[Point3]]): SurfaceRenderer = new SurfaceRenderer {
    private def getColorSeq(numPoints: Int): Seq[Color] =
      if (numPoints <= DefaultColors.nicePalette.length) DefaultColors.nicePalette.take(numPoints)
      else Color.stream.take(numPoints)

    def getBySafe[T](data: Seq[T])(f: T => Option[Double]): Option[Bounds] = {
      val mapped = data.map(f).filterNot(_.forall(_.isNaN)).flatten
      Bounds.get(mapped)
    }

    override def legendContext: Option[LegendContext] = {
      val colors = getColorSeq(points.length)

      getBySafe(points)(_.headOption.map(_.z)).map { bs =>
        val bar = ScaledColorBar(colors, bs.min, bs.max)
        LegendContext(
          elements = (0 until bar.nColors).map { c => Rect(1, 1).filled(bar.getColor(c)) },
          labels = (0 until bar.nColors).map { c => Text(math.round(bar.colorValue(c)).toString) },
          defaultStyle = LegendStyle.Gradient
        )
      }
    }

    def render(plot: Plot, extent: Extent, surface: Seq[Point3]): Drawable = {
      val surfaceRenderer = getBySafe(points)(_.headOption.map(_.z)).map { bs =>
        val bar = ScaledColorBar(getColorSeq(points.length), bs.min, bs.max)
        densityColorContours(strokeWidth, bar)(points)
      }.getOrElse(contours(strokeWidth))
      surfaceRenderer.render(plot, extent, surface)
    }
  }

  def densityColorContours(
    strokeWidth: Double,
    bar: ScaledColorBar
  )(points: Seq[Seq[Point3]]): SurfaceRenderer = new SurfaceRenderer {
    def render(plot: Plot, extent: Extent, points: Seq[Point3]): Drawable = {
      points.headOption.map(p => contours(strokeWidth, bar.getColor(p.z))
        .render(plot, extent, points)
      )
      .getOrElse(EmptyDrawable())
    }
  }
}
