package com.cibo.evilplot.plot.components

import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent}
import com.cibo.evilplot.plot.Plot

case class Pad(
  position: Position,
  pad: Double
) extends PlotComponent {
  override def size[T](plot: Plot[T]): Extent = Extent(pad, pad)
  def render[T](plot: Plot[T], extent: Extent): Drawable = EmptyDrawable(size(plot))
}

trait PadImplicits[T] {
  protected val plot: Plot[T]

  def padTop(size: Double): Plot[T] = plot :+ Pad(Position.Top, size)
  def padBottom(size: Double): Plot[T] = plot :+ Pad(Position.Bottom, size)
  def padLeft(size: Double): Plot[T] = plot :+ Pad(Position.Left, size)
  def padRight(size: Double): Plot[T] = plot :+ Pad(Position.Right, size)
}
