package com.cibo.evilplot.plot.renderers

import com.cibo.evilplot.colors.{Color, DefaultColors, ScaledColorBar}
import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent, Path, Rect, StrokeStyle, Text}
import com.cibo.evilplot.numeric.{Bounds, Point, Point3}
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.{LegendContext, LegendStyle, Plot}

trait SurfaceRenderer extends PlotElementRenderer[Seq[Seq[Point3]]] {
  def legendContext: LegendContext = LegendContext.empty
  def render(plot: Plot, extent: Extent, surface: Seq[Seq[Point3]]): Drawable
}

object SurfaceRenderer {

  def contours(
    color: Option[Color] = None
  )(implicit theme: Theme): SurfaceRenderer = new SurfaceRenderer {
    def render(plot: Plot, extent: Extent, surface: Seq[Seq[Point3]]): Drawable = {
      surface.map(pathpts => Path(pathpts.map(p => Point(p.x, p.y)), theme.elements.strokeWidth))
        .group
        .colored(color.getOrElse(theme.colors.path))
    }
  }

  def densityColorContours(points: Seq[Seq[Seq[Point3]]])(implicit theme: Theme): SurfaceRenderer = new SurfaceRenderer {
    private def getColorSeq(numPoints: Int): Seq[Color] =
      if (numPoints <= DefaultColors.nicePalette.length) DefaultColors.nicePalette.take(numPoints)
      else Color.stream.take(numPoints)

    def getBySafe[T](data: Seq[T])(f: T => Option[Double]): Option[Bounds] = {
      val mapped = data.map(f).filterNot(_.forall(_.isNaN)).flatten
      Bounds.get(mapped)
    }

    override def legendContext: LegendContext = {
      val colors = getColorSeq(points.length)
      getBySafe(points)(_.headOption.flatMap(_.headOption.map(_.z))).map { bs =>
        val bar = ScaledColorBar(colors, bs.min, bs.max)
        LegendContext.fromColorBar(bar)(theme)
      }.getOrElse(LegendContext.empty)
    }

    def render(plot: Plot, extent: Extent, surface: Seq[Seq[Point3]]): Drawable = {
      val surfaceRenderer = getBySafe(points)(_.headOption.flatMap(_.headOption.map(_.z))).map { bs =>
        val bar = ScaledColorBar(getColorSeq(points.length), bs.min, bs.max)
        densityColorContours(bar)(points)
      }.getOrElse(contours())
      surfaceRenderer.render(plot, extent, surface)
    }
  }

  def densityColorContours(
    bar: ScaledColorBar
  )(points: Seq[Seq[Seq[Point3]]])(implicit theme: Theme): SurfaceRenderer = new SurfaceRenderer {
    def render(plot: Plot, extent: Extent, surface: Seq[Seq[Point3]]): Drawable = {
      surface.headOption.map(pts =>
        contours(Some(pts.headOption.fold(theme.colors.path)(p => bar.getColor(p.z))))
        .render(plot, extent, surface)
      )
      .getOrElse(EmptyDrawable())
    }
  }
}
