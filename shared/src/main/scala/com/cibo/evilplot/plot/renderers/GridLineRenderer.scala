package com.cibo.evilplot.plot.renderers

import com.cibo.evilplot.geometry.{Drawable, Extent, Line}
import com.cibo.evilplot.plot.aesthetics.Theme

trait GridLineRenderer {
  def render(extent: Extent, label: String): Drawable
}

object GridLineRenderer {

  val defaultThickness: Double = 1.0

  def xGridLineRenderer(
    thickness: Double = defaultThickness
  )(implicit theme: Theme): GridLineRenderer = new GridLineRenderer {
    def render(extent: Extent, label: String): Drawable = {
      Line(extent.height, thickness).colored(theme.colors.gridLine).rotated(90)
    }
  }

  def yGridLineRenderer(
    thickness: Double = defaultThickness
  )(implicit theme: Theme): GridLineRenderer = new GridLineRenderer {
    def render(extent: Extent, label: String): Drawable = {
      Line(extent.width, thickness).colored(theme.colors.gridLine)
    }
  }
}
