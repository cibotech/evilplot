package com.cibo.evilplot.plot.renderers

import com.cibo.evilplot.geometry.{Drawable, Extent, Line}
import com.cibo.evilplot.plot.aesthetics.Theme

trait GridLineRenderer {
  def render(extent: Extent, label: String): Drawable
}

object GridLineRenderer {

  def xGridLineRenderer()(implicit theme: Theme): GridLineRenderer = new GridLineRenderer {
    def render(extent: Extent, label: String): Drawable = {
      Line(extent.height, theme.elements.gridLineSize).colored(theme.colors.gridLine).rotated(90)
    }
  }

  def yGridLineRenderer()(implicit theme: Theme): GridLineRenderer = new GridLineRenderer {
    def render(extent: Extent, label: String): Drawable = {
      Line(extent.width, theme.elements.gridLineSize).colored(theme.colors.gridLine)
    }
  }
}
