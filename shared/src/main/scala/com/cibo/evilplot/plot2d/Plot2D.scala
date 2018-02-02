package com.cibo.evilplot.plot2d

import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent, Translate}
import com.cibo.evilplot.numeric.{Bounds, Point}

final case class Plot2D[T] private[evilplot] (
  data: T, // Raw data
  xbounds: Bounds, // x bounds of the raw data
  ybounds: Bounds, // y bounds of the raw data
  private[plot2d] val renderer: (Plot2D[T], Extent) => Drawable,
  private[plot2d] val xtransform: Plot2D.Transformer[T] = Plot2D.DefaultXTransformer[T](),
  private[plot2d] val ytransform: Plot2D.Transformer[T] = Plot2D.DefaultYTransformer[T](),
  private[plot2d] val annotations: Seq[PlotAnnotation[T]] = Seq.empty // Annotations (ordered inside out)
) {

  private[plot2d] def inBounds(point: Point): Boolean = xbounds.isInBounds(point.x) && ybounds.isInBounds(point.y)

  private[plot2d] def :+(annotation: PlotAnnotation[T]): Plot2D[T] = copy(annotations = annotations :+ annotation)
  private[plot2d] def +:(annotation: PlotAnnotation[T]): Plot2D[T] = copy(annotations = annotation +: annotations)

  def xbounds(newBounds: Bounds): Plot2D[T] = copy(xbounds = newBounds)
  def ybounds(newBounds: Bounds): Plot2D[T] = copy(ybounds = newBounds)

  // Get the offset of the plot area.
  private[plot2d] lazy val plotOffset: Point = {

    // y offset for sides due to the annotations at the top.
    val yoffset = annotations.filter(_.position == PlotAnnotation.Top).map(_.size(this).height).sum

    // x offset for top/bottom due to the annotations on the left.
    val xoffset = annotations.filter(_.position == PlotAnnotation.Left).map(_.size(this).width).sum

    Point(xoffset, yoffset)
  }

  // Get the size of the actual plot area.
  // Annotations on the left/right reduce the width of the plot area and
  // annotations on the top/bottom reduce the height of the plot area.
  private[plot2d] def plotExtent(extent: Extent): Extent = {
    annotations.foldLeft(extent) { (oldExtent, annotation) =>
      val size = annotation.size(this)
      annotation.position match {
        case PlotAnnotation.Top        => oldExtent.copy(height = oldExtent.height - size.height)
        case PlotAnnotation.Bottom     => oldExtent.copy(height = oldExtent.height - size.height)
        case PlotAnnotation.Left       => oldExtent.copy(width = oldExtent.width - size.width)
        case PlotAnnotation.Right      => oldExtent.copy(width = oldExtent.width - size.width)
        case PlotAnnotation.Overlay    => oldExtent
        case PlotAnnotation.Background => oldExtent
      }
    }
  }

  def render(extent: Extent = Plot2D.defaultExtent): Drawable = {
    val pextent = plotExtent(extent)

    // Render annotations.
    val empty: Drawable = EmptyDrawable()
    val top: Drawable = annotations.filter(_.position == PlotAnnotation.Top).reverse.foldLeft(empty) { (d, a) =>
      Translate(a.render(this, pextent), x = plotOffset.x, y = d.extent.height) behind d
    }
    val bottom = annotations.filter { a =>
      a.position == PlotAnnotation.Bottom
    }.reverse.foldLeft((extent.height, empty)) { case ((y, d), a) =>
      val rendered = a.render(this, pextent)
      val newY = y - rendered.extent.height
      (newY, Translate(rendered, x = plotOffset.x, y = newY) behind d)
    }._2
    val left = annotations.filter(_.position == PlotAnnotation.Left).foldLeft(empty) { (d, a) =>
      Translate(a.render(this, pextent), y = plotOffset.y) beside d
    }
    val right = annotations.filter { a =>
      a.position == PlotAnnotation.Right
    }.reverse.foldLeft((extent.width, empty)) { case ((x, d), a) =>
      val rendered = a.render(this, pextent)
      val newX = x - rendered.extent.width
      (newX, Translate(rendered, x = newX, y = plotOffset.y) behind d)
    }._2
    val overlays = annotations.filter(_.position == PlotAnnotation.Overlay).map { a =>
      a.render(this, pextent)
    }.group
    val backgrounds = annotations.filter(_.position == PlotAnnotation.Background).map { a =>
      a.render(this, pextent)
    }.group

    // Render the plot.
    val renderedPlot = backgrounds behind renderer(this, pextent) behind overlays
    Translate(renderedPlot, x = plotOffset.x, y = plotOffset.y) behind top behind bottom behind left behind right
  }
}

object Plot2D {
  val defaultExtent: Extent = Extent(800, 600)

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
