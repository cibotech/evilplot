package com.cibo.evilplot.plot.renderers

import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent}
import com.cibo.evilplot.plot.Plot

/** Renderer for non-plot area components of a plot (labels, etc.). */
trait ComponentRenderer[T] {
  /** Render components that go in front of and around the plot area. */
  def renderFront(plot: Plot[T], extent: Extent): Drawable

  /** Render components that go behind the plot area. */
  def renderBack(plot: Plot[T], extent: Extent): Drawable
}

object ComponentRenderer {

  case class Default[T]() extends ComponentRenderer[T] {

    private val empty: Drawable = EmptyDrawable()

    private def renderTop(plot: Plot[T], extent: Extent): Drawable = {
      val pextent = plot.plotExtent(extent)
      plot.topComponents.reverse.foldLeft(empty) { (d, a) =>
        a.render(plot, pextent).translate(x = plot.plotOffset.x, y = d.extent.height) behind d
      }
    }

    private def renderBottom(plot: Plot[T], extent: Extent): Drawable = {
      val pextent = plot.plotExtent(extent)
      plot.bottomComponents.reverse.foldLeft((extent.height, empty)) { case ((y, d), a) =>
        val rendered = a.render(plot, pextent)
        val newY = y - rendered.extent.height
        (newY, rendered.translate(x = plot.plotOffset.x, y = newY) behind d)
      }._2
    }

    private def renderLeft(plot: Plot[T], extent: Extent): Drawable = {
      val pextent = plot.plotExtent(extent)
      plot.leftComponents.foldLeft(empty) { (d, c) =>
        c.render(plot, pextent).translate(y = plot.plotOffset.y) beside d
      }
    }

    private def renderRight(plot: Plot[T], extent: Extent): Drawable = {
      val pextent = plot.plotExtent(extent)
      plot.rightComponents.reverse.foldLeft((extent.width, empty)) { case ((x, d), a) =>
        val rendered = a.render(plot, pextent)
        val newX = x - rendered.extent.width
        (newX, rendered.translate(x = newX, y = plot.plotOffset.y) behind d)
      }._2
    }

    private def renderOverlay(plot: Plot[T], extent: Extent): Drawable = {
      val pextent = plot.plotExtent(extent)
      plot.overlayComponents.map { a =>
        a.render(plot, pextent).translate(x = plot.plotOffset.x, y = plot.plotOffset.y)
      }.group
    }

    def renderFront(plot: Plot[T], extent: Extent): Drawable = {
      renderOverlay(plot, extent)
        .behind(renderLeft(plot, extent))
        .behind(renderRight(plot, extent))
        .behind(renderBottom(plot, extent))
        .behind(renderTop(plot, extent))
    }

    def renderBack(plot: Plot[T], extent: Extent): Drawable = {
      val pextent = plot.plotExtent(extent)
      plot.backgroundComponents.map { a =>
        a.render(plot, pextent).translate(x = plot.plotOffset.x, y = plot.plotOffset.y)
      }.group
    }
  }
}
