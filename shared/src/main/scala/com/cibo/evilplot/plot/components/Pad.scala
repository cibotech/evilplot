package com.cibo.evilplot.plot.components

import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent}
import com.cibo.evilplot.plot.Plot

case class Pad(
  position: Position,
  pad: Double
) extends PlotComponent {
  override def size(plot: Plot): Extent = Extent(pad, pad)
  def render(plot: Plot, extent: Extent): Drawable = EmptyDrawable(size(plot))
}

trait PadImplicits {
  protected val plot: Plot

  def padTop(size: Double): Plot = plot :+ Pad(Position.Top, size)
  def padBottom(size: Double): Plot = plot :+ Pad(Position.Bottom, size)
  def padLeft(size: Double): Plot = plot :+ Pad(Position.Left, size)
  def padRight(size: Double): Plot = plot :+ Pad(Position.Right, size)
}
