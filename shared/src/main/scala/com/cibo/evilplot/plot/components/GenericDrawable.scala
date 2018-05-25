package com.cibo.evilplot.plot.components

import com.cibo.evilplot.geometry.{Drawable, Extent}
import com.cibo.evilplot.plot.Plot
import com.cibo.evilplot.plot.aesthetics.Theme

case class GenericDrawable(
  position: Position,
  f: (Extent => Drawable),
  size: Extent,
  x: Double = 0,
  y: Double = 0,
) extends PlotComponent {

  override def size(plot: Plot): Extent = size

  def render(plot: Plot, extent: Extent)(implicit theme: Theme): Drawable = {
    val drawable = f(extent)
    position match {
      case Position.Top | Position.Bottom =>
        val xoffset = plot.xtransform(plot, extent)(x) - (drawable.extent.width / 2)
        drawable.translate(x = xoffset)
      case Position.Left | Position.Right =>
        val yoffset = plot.ytransform(plot, extent)(y) - (drawable.extent.height / 2)
        drawable.translate(y = yoffset)
      case Position.Overlay | Position.Background =>
        val yoffset = plot.ytransform(plot, extent)(y) - (drawable.extent.height / 2)
        val xoffset = plot.xtransform(plot, extent)(x) - (drawable.extent.width / 2)
        drawable.translate(x = xoffset, y = yoffset)
      case _ => throw new IllegalArgumentException("`position` must be one of Top, Bottom, Left, or Right")
    }
  }
}
