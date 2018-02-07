package com.cibo.evilplot.plot.renderers

import com.cibo.evilplot.colors.{Color, HTMLNamedColors}
import com.cibo.evilplot.geometry.{Drawable, Extent, Line}

trait GridLineRenderer {
  def render(label: String, extent: Extent): Drawable
}

object GridLineRenderer {

  val defaultThickness: Double = 1.0
  val defaultColor: Color = HTMLNamedColors.white

  def xGridLineRenderer(
    thickness: Double = defaultThickness,
    color: Color = defaultColor
  ): GridLineRenderer = new GridLineRenderer {
    def render(label: String, extent: Extent): Drawable = {
      Line(extent.height, thickness).colored(color).rotated(90)
    }
  }

  def yGridLineRenderer(
    thickness: Double = defaultThickness,
    color: Color = defaultColor
  ): GridLineRenderer = new GridLineRenderer {
    def render(label: String, extent: Extent): Drawable = {
      Line(extent.width, thickness).colored(color)
    }
  }
}
