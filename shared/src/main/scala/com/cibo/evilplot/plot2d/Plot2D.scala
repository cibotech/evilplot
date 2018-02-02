package com.cibo.evilplot.plot2d

import com.cibo.evilplot.colors.DefaultColors
import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent, Rect, Translate}
import com.cibo.evilplot.numeric.Bounds

final case class Plot2D[T] private[evilplot](
  data: T, // Raw data
  private[plot2d] val xbounds: Bounds, // x bounds of the raw data
  private[plot2d] val ybounds: Bounds, // y bounds of the raw data
  private[plot2d] val renderer: (Plot2D[T], Extent) => Drawable,
  private[plot2d] val xtransform: Plot2D.Transformer[T] = Plot2D.DefaultXTransformer[T](),
  private[plot2d] val ytransform: Plot2D.Transformer[T] = Plot2D.DefaultYTransformer[T](),
  private[plot2d] val annotations: Seq[PlotAnnotation[T]] = Seq.empty, // Annotations (ordered inside out)
  private[plot2d] val background: Extent => Drawable = Plot2D.defaultBackground
) {
  def render(extent: Extent = Plot2D.defaultExtent): Drawable = {

    // Get the size of the actual plot area.
    // Annotations on the left/right reduce the width of the plot area and
    // annotations on the top/bottom reduce the height of the plot area.
    val plotExtent = annotations.foldLeft(extent) { (oldExtent, annotation) =>
      val size = annotation.size(this)
      annotation.position match {
        case PlotAnnotation.Top     => oldExtent.copy(height = oldExtent.height - size.height)
        case PlotAnnotation.Bottom  => oldExtent.copy(height = oldExtent.height - size.height)
        case PlotAnnotation.Left    => oldExtent.copy(width = oldExtent.width - size.width)
        case PlotAnnotation.Right   => oldExtent.copy(width = oldExtent.width - size.width)
        case PlotAnnotation.Overlay => oldExtent
      }
    }

    // y offset for sides due to the annotations at the top.
    val yoffset = annotations.filter(_.position == PlotAnnotation.Top).map(_.size(this).height).sum

    // x offset for top/bottom due to the annotations on the left.
    val xoffset = annotations.filter(_.position == PlotAnnotation.Left).map(_.size(this).width).sum

    // Render annotations.
    val empty: Drawable = EmptyDrawable()
    val top: Drawable = annotations.filter(_.position == PlotAnnotation.Top).reverse.foldLeft(empty) { (d, a) =>
      Translate(a.render(this, plotExtent), x = xoffset, y = d.extent.height) behind d
    }
    val bottom = annotations.filter { a =>
      a.position == PlotAnnotation.Bottom
    }.reverse.foldLeft((extent.height, empty)) { case ((y, d), a) =>
      val rendered = a.render(this, plotExtent)
      val newY = y - rendered.extent.height
      (newY, Translate(rendered, x = xoffset, y = newY) behind d)
    }._2
    val left = annotations.filter(_.position == PlotAnnotation.Left).foldLeft(empty) { (d, a) =>
      Translate(a.render(this, plotExtent), y = yoffset) beside d
    }
    val right = annotations.filter { a =>
      a.position == PlotAnnotation.Right
    }.reverse.foldLeft((extent.width, empty)) { case ((x, d), a) =>
      val rendered = a.render(this, plotExtent)
      val newX = x - rendered.extent.width
      (newX, Translate(rendered, x = newX, y = yoffset) behind d)
    }._2
    val overlays = annotations.filter(_.position == PlotAnnotation.Overlay).map { a =>
      a.render(this, plotExtent)
    }.group

    // Render the plot.
    val renderedPlot = background(plotExtent) behind renderer(this, plotExtent) behind overlays
    Translate(renderedPlot, x = xoffset, y = yoffset) behind top behind bottom behind left behind right
  }
}

object Plot2D {
  val defaultExtent: Extent = Extent(800, 600)

  def defaultBackground(extent: Extent): Drawable = Rect(extent) filled DefaultColors.backgroundColor

  private[plot2d] trait Transformer[T] {
    def apply(plot: Plot2D[T], extent: Extent): Double => Double
  }

  private[plot2d] case class DefaultXTransformer[T]() extends Transformer[T] {
    def apply(plot: Plot2D[T], extent: Extent): Double => Double =
      (x: Double) => (x - plot.xbounds.min) * extent.width / plot.xbounds.range
  }

  private[plot2d] case class DefaultYTransformer[T]() extends Transformer[T] {
    def apply(plot: Plot2D[T], extent: Extent): Double => Double =
      (y: Double) => extent.height - (y - plot.ybounds.min) * extent.height / plot.ybounds.range
  }
}
