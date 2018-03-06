package com.cibo.evilplot.plot.renderers

import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent}
import com.cibo.evilplot.plot.Plot
import com.cibo.evilplot.plot.aesthetics.Theme

/** Renderer for non-plot area components of a plot (labels, etc.). */
trait ComponentRenderer {
  /** Render components that go in front of and around the plot area. */
  def renderFront(plot: Plot, extent: Extent)(implicit theme: Theme): Drawable

  /** Render components that go behind the plot area. */
  def renderBack(plot: Plot, extent: Extent)(implicit theme: Theme): Drawable
}

object ComponentRenderer {

  case class Default() extends ComponentRenderer {

    private val empty: Drawable = EmptyDrawable()

    private def renderTop(plot: Plot, extent: Extent)(implicit theme: Theme): Drawable = {
      val plotExtent = plot.plotExtent(extent)
      plot.topComponents.reverse.foldLeft(empty) { (d, c) =>
        val componentExtent = plotExtent.copy(height = c.size(plot).height)
        c.render(plot, componentExtent, 0, 0).translate(x = plot.plotOffset.x, y = d.extent.height) behind d
      }
    }

    private def renderBottom(plot: Plot, extent: Extent)(implicit theme: Theme): Drawable = {
      val plotExtent = plot.plotExtent(extent)
      plot.bottomComponents.reverse.foldLeft((extent.height, empty)) { case ((y, d), c) =>
        val componentExtent = plotExtent.copy(height = c.size(plot).height)
        val rendered = c.render(plot, plotExtent, 0, 0)
        val newY = y - rendered.extent.height
        (newY, rendered.translate(x = plot.plotOffset.x, y = newY) behind d)
      }._2
    }

    private def renderLeft(plot: Plot, extent: Extent)(implicit theme: Theme): Drawable = {
      val plotExtent = plot.plotExtent(extent)
      plot.leftComponents.foldLeft(empty) { (d, c) =>
        val componentExtent = plotExtent.copy(width = c.size(plot).width)
        c.render(plot, componentExtent, 0, 0).translate(y = plot.plotOffset.y) beside d
      }
    }

    private def renderRight(plot: Plot, extent: Extent)(implicit theme: Theme): Drawable = {
      val plotExtent = plot.plotExtent(extent)
      plot.rightComponents.reverse.foldLeft((extent.width, empty)) { case ((x, d), c) =>
        val componentExtent = plotExtent.copy(width = c.size(plot).width)
        val rendered = c.render(plot, componentExtent, 0, 0)
        val newX = x - rendered.extent.width
        (newX, rendered.translate(x = newX, y = plot.plotOffset.y) behind d)
      }._2
    }

    private def renderOverlay(plot: Plot, extent: Extent)(implicit theme: Theme): Drawable = {
      val plotExtent = plot.plotExtent(extent)
      plot.overlayComponents.map { a =>
        a.render(plot, plotExtent, 0, 0).translate(x = plot.plotOffset.x, y = plot.plotOffset.y)
      }.group
    }

    def renderFront(plot: Plot, extent: Extent)(implicit theme: Theme): Drawable = {
      renderOverlay(plot, extent)
        .behind(renderLeft(plot, extent))
        .behind(renderRight(plot, extent))
        .behind(renderBottom(plot, extent))
        .behind(renderTop(plot, extent))
    }

    def renderBack(plot: Plot, extent: Extent)(implicit theme: Theme): Drawable = {
      val plotExtent = plot.plotExtent(extent)
      plot.backgroundComponents.map { a =>
        a.render(plot, plotExtent, 0, 0).translate(x = plot.plotOffset.x, y = plot.plotOffset.y)
      }.group
    }
  }
}
