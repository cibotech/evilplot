package com.cibo.evilplot.plot.renderers

import com.cibo.evilplot.colors.{Color, DefaultColors, ScaledColorBar}
import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent, Path, Rect, StrokeStyle, Text}
import com.cibo.evilplot.numeric.{Bounds, Point, Point3}
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.{LegendContext, LegendStyle, Plot}

trait SurfaceRenderer extends PlotElementRenderer[Seq[Point3]] {
  def legendContext: LegendContext = LegendContext.empty
  def render(plot: Plot, extent: Extent, surface: Seq[Point3])(implicit theme: Theme): Drawable
}

object SurfaceRenderer {

  def contours(color: Option[Color] = None)(implicit theme: Theme): SurfaceRenderer = new SurfaceRenderer {
    def render(plot: Plot, extent: Extent, surface: Seq[Point3])(implicit theme: Theme): Drawable = {
      surface.grouped(2).map { seg =>
        Path(seg.map(p => Point(p.x, p.y)), theme.elements.strokeWidth)
      }.toSeq.group colored color.getOrElse(theme.colors.path)
    }
  }

  def densityColorContours(
    theme: Theme
  )(points: Seq[Seq[Point3]]): SurfaceRenderer = new SurfaceRenderer {
    private def getColorSeq(numPoints: Int): Seq[Color] =
      if (numPoints <= DefaultColors.nicePalette.length) DefaultColors.nicePalette.take(numPoints)
      else Color.stream.take(numPoints)

    def getBySafe[T](data: Seq[T])(f: T => Option[Double]): Option[Bounds] = {
      val mapped = data.map(f).filterNot(_.forall(_.isNaN)).flatten
      Bounds.get(mapped)
    }

    override def legendContext: LegendContext = {
      val colors = getColorSeq(points.length)

      getBySafe(points)(_.headOption.map(_.z)).map { bs =>
        val bar = ScaledColorBar(colors, bs.min, bs.max)
        LegendContext.fromColorBar(bar)(theme)
      }.getOrElse(LegendContext.empty)
    }

    def render(plot: Plot, extent: Extent, surface: Seq[Point3])(implicit theme: Theme): Drawable = {
      val surfaceRenderer = getBySafe(points)(_.headOption.map(_.z)).map { bs =>
        val bar = ScaledColorBar(getColorSeq(points.length), bs.min, bs.max)
        densityColorContours(theme.elements.strokeWidth, bar)(points)
      }.getOrElse(contours())
      surfaceRenderer.render(plot, extent, surface)
    }
  }

  def densityColorContours(
    strokeWidth: Double,
    bar: ScaledColorBar
  )(points: Seq[Seq[Point3]]): SurfaceRenderer = new SurfaceRenderer {
    def render(plot: Plot, extent: Extent, points: Seq[Point3])(implicit theme: Theme): Drawable = {
      points.headOption.map(p => contours(Some(bar.getColor(p.z)))
        .render(plot, extent, points)
      )
      .getOrElse(EmptyDrawable())
    }
  }
}
