package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry._
import com.cibo.evilplot.numeric.{Bounds, Point}
import com.cibo.evilplot.plot.components.{PlotComponent, Position}

final case class Plot[T] private[evilplot] (
  data: T, // Raw data
  xbounds: Bounds, // x bounds of the raw data
  ybounds: Bounds, // y bounds of the raw data
  private[plot] val renderer: (Plot[T], Extent) => Drawable,
  private[plot] val componentRenderer: (Plot[T], Extent) => (Drawable, Drawable) =
    (p: Plot[T], e: Extent) => Plot.defaultComponentRenderer(p, e),
  private[plot] val xtransform: Plot.Transformer = Plot.DefaultXTransformer(),
  private[plot] val ytransform: Plot.Transformer = Plot.DefaultYTransformer(),
  private[plot] val xfixed: Boolean = false,    // Set if x bounds are fixed.
  private[plot] val yfixed: Boolean = false,    // Set if y bounds are fixed.
  private[plot] val components: Seq[PlotComponent] = Seq.empty // Components (ordered inside out)
) {
  private[plot] def inBounds(point: Point): Boolean = xbounds.isInBounds(point.x) && ybounds.isInBounds(point.y)

  private[plot] def :+(component: PlotComponent): Plot[T] = copy(components = components :+ component)
  private[plot] def +:(component: PlotComponent): Plot[T] = copy(components = component +: components)

  def xbounds(newBounds: Bounds): Plot[T] = copy(xbounds = newBounds, xfixed = true)
  def xbounds(lower: Double, upper: Double): Plot[T] = xbounds(Bounds(lower, upper))
  def ybounds(newBounds: Bounds): Plot[T] = copy(ybounds = newBounds, yfixed = true)
  def ybounds(lower: Double, upper: Double): Plot[T] = ybounds(Bounds(lower, upper))

  private[plot] def updateBounds(xb: Bounds, yb: Bounds): Plot[T] = copy(xbounds = xb, ybounds = yb)

  def setXTransform(xt: Plot.Transformer, fixed: Boolean): Plot[T] = copy(xtransform = xt, xfixed = fixed)
  def setYTransform(yt: Plot.Transformer, fixed: Boolean): Plot[T] = copy(ytransform = yt, yfixed = fixed)

  lazy val topComponents: Seq[PlotComponent] = components.filter(_.position == Position.Top)
  lazy val bottomComponents: Seq[PlotComponent] = components.filter(_.position == Position.Bottom)
  lazy val leftComponents: Seq[PlotComponent] = components.filter(_.position == Position.Left)
  lazy val rightComponents: Seq[PlotComponent] = components.filter(_.position == Position.Right)
  lazy val backgroundComponents: Seq[PlotComponent] = components.filter(_.position == Position.Background)
  lazy val overlayComponents: Seq[PlotComponent] = components.filter(_.position == Position.Overlay)

  // Get the offset of the plot area.
  private[plot] lazy val plotOffset: Point = {

    // y offset for sides due to the annotations at the top.
    val yoffset = topComponents.map(_.size(this).height).sum

    // x offset for top/bottom due to the annotations on the left.
    val xoffset = leftComponents.map(_.size(this).width).sum

    Point(xoffset, yoffset)
  }

  // Get the size of the actual plot area.
  // Annotations on the left/right reduce the width of the plot area and
  // annotations on the top/bottom reduce the height of the plot area.
  private[plot] def plotExtent(extent: Extent): Extent = {
    components.foldLeft(extent) { (oldExtent, annotation) =>
      val size = annotation.size(this)
      annotation.position match {
        case Position.Top        => oldExtent.copy(height = oldExtent.height - size.height)
        case Position.Bottom     => oldExtent.copy(height = oldExtent.height - size.height)
        case Position.Left       => oldExtent.copy(width = oldExtent.width - size.width)
        case Position.Right      => oldExtent.copy(width = oldExtent.width - size.width)
        case Position.Overlay    => oldExtent
        case Position.Background => oldExtent
      }
    }
  }

  def render(extent: Extent = Plot.defaultExtent): Drawable = {
    val (overlays, backgrounds) = componentRenderer(this, extent)
    val renderedPlot = renderer(this, plotExtent(extent)).translate(x = plotOffset.x, y = plotOffset.y)
    backgrounds behind renderedPlot behind overlays
  }
}

object Plot {
  val defaultExtent: Extent = Extent(800, 600)

  private[plot] trait Transformer {
    def apply(plot: Plot[_], plotExtent: Extent): Double => Double
  }

  private val empty: Drawable = EmptyDrawable()

  private[plot] case class DefaultXTransformer() extends Transformer {
    def apply(plot: Plot[_], plotExtent: Extent): Double => Double =
      (x: Double) => (x - plot.xbounds.min) * plotExtent.width / plot.xbounds.range
  }

  private[plot] case class DefaultYTransformer() extends Transformer {
    def apply(plot: Plot[_], plotExtent: Extent): Double => Double =
      (y: Double) => plotExtent.height - (y - plot.ybounds.min) * plotExtent.height / plot.ybounds.range
  }

  private[plot] def topComponentRenderer[T](plot: Plot[T], extent: Extent): Drawable = {
    val pextent = plot.plotExtent(extent)
    plot.topComponents.reverse.foldLeft(empty) { (d, a) =>
      a.render(plot, pextent).translate(x = plot.plotOffset.x, y = d.extent.height) behind d
    }
  }

  private[plot] def bottomComponentRenderer[T](plot: Plot[T], extent: Extent): Drawable = {
    val pextent = plot.plotExtent(extent)
    plot.bottomComponents.reverse.foldLeft((extent.height, empty)) { case ((y, d), a) =>
      val rendered = a.render(plot, pextent)
      val newY = y - rendered.extent.height
      (newY, rendered.translate(x = plot.plotOffset.x, y = newY) behind d)
    }._2
  }

  private[plot] def leftComponentRenderer[T](plot: Plot[T], extent: Extent): Drawable = {
    val pextent = plot.plotExtent(extent)
    plot.leftComponents.foldLeft(empty) { (d, c) =>
      c.render(plot, pextent).translate(y = plot.plotOffset.y) beside d
    }
  }

  private[plot] def rightComponentRenderer[T](plot: Plot[T], extent: Extent): Drawable = {
    val pextent = plot.plotExtent(extent)
    plot.rightComponents.foldLeft((extent.width, empty)) { case ((x, d), a) =>
      val rendered = a.render(plot, pextent)
      val newX = x - rendered.extent.width
      (newX, rendered.translate(x = newX, y = plot.plotOffset.y) behind d)
    }._2
  }

  private[plot] def overlayComponentRenderer[T](plot: Plot[T], extent: Extent): Drawable = {
    val pextent = plot.plotExtent(extent)
    plot.overlayComponents.map { a =>
      a.render(plot, pextent).translate(x = plot.plotOffset.x, y = plot.plotOffset.y)
    }.group
  }

  private[plot] def backgroundComponentRenderer[T](plot: Plot[T], extent: Extent): Drawable = {
    val pextent = plot.plotExtent(extent)
    plot.backgroundComponents.map { a =>
      a.render(plot, pextent).translate(x = plot.plotOffset.x, y = plot.plotOffset.y)
    }.group
  }

  private[plot] def defaultComponentRenderer[T](plot: Plot[T], extent: Extent): (Drawable, Drawable) = {
    val overlays = overlayComponentRenderer(plot, extent)
      .behind(leftComponentRenderer(plot, extent))
      .behind(rightComponentRenderer(plot, extent))
      .behind(bottomComponentRenderer(plot, extent))
      .behind(topComponentRenderer(plot, extent))
    val backgrounds = backgroundComponentRenderer(plot, extent)
    (overlays, backgrounds)
  }

  // Combine the bounds for multiple plots (taking the widest).
  private[plot] def combineBounds(bounds: Seq[Bounds]): Bounds = {
    Bounds(bounds.minBy(_.min).min, bounds.maxBy(_.max).max)
  }

  // Force all plots to have the same size plot area.
  private[plot] def padPlots(
    plots: Seq[Seq[Plot[_]]],
    extent: Extent,
    padRight: Double,
    padBottom: Double
  ): Seq[Seq[Plot[_]]] = {

    // First we get the offsets of all subplots.  By selecting the largest
    // offset, we can pad all plots to start at the same location.
    val plotOffsets = plots.flatMap(_.map(_.plotOffset))
    val xoffset = plotOffsets.maxBy(_.x).x
    val yoffset = plotOffsets.maxBy(_.y).y

    // Update the plots with their offsets.
    val offsetPlots = plots.map { row =>
      row.map { subplot =>
        subplot.padTop(yoffset - subplot.plotOffset.y).padLeft(xoffset - subplot.plotOffset.x)
      }
    }

    // Now the subplots all start at the same place, so we need to ensure they all
    // end at the same place.  We do this by computing the maximum right and bottom
    // fill amounts and then padding.
    val plotAreas = offsetPlots.flatMap(_.map(_.plotExtent(extent)))
    val minWidth = plotAreas.minBy(_.width).width
    val minHeight = plotAreas.minBy(_.height).height
    val rowCount = offsetPlots.length
    offsetPlots.zipWithIndex.map { case (row, rowIndex) =>
      val columnCount = row.length
      row.zipWithIndex.map { case (subplot, columnIndex) =>
        val extraRight = if (columnIndex + 1 < columnCount) padRight else 0
        val extraBottom = if (rowIndex + 1 < rowCount) padBottom else 0
        val pe = subplot.plotExtent(extent)
        val fillx = pe.width - minWidth + extraRight
        val filly = pe.height - minHeight + extraBottom
        subplot.padRight(fillx).padBottom(filly)
      }
    }
  }
}
