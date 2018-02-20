package com.cibo.evilplot.plot.components

import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent}
import com.cibo.evilplot.plot.Plot

case class Pad(
  position: Position,
  pad: Double
) extends PlotComponent {
  override def size(plot: Plot): Extent = Extent(pad, pad)
  def render(plot: Plot, extent: Extent): Drawable = EmptyDrawable().resize(size(plot))
}

trait PadImplicits {
  protected val plot: Plot

  def padTop(size: Double): Plot = if (size > 0) plot :+ Pad(Position.Top, size) else plot
  def padBottom(size: Double): Plot = if (size > 0) plot :+ Pad(Position.Bottom, size) else plot
  def padLeft(size: Double): Plot = if (size > 0) plot :+ Pad(Position.Left, size) else plot
  def padRight(size: Double): Plot = if (size > 0) plot :+ Pad(Position.Right, size) else plot
}
