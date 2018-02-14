package com.cibo.evilplot.plot.components

import com.cibo.evilplot.geometry.{Drawable, Extent, Text}
import com.cibo.evilplot.plot.Plot

/** A plot label.
  * @param position The position of this component.
  * @param f A function to create the label for the given extent.
  * @param minExtent The minimum extent.
  */
case class Label(
  position: Position,
  f: Extent => Drawable,
  minExtent: Extent
) extends PlotComponent {
  override def size(plot: Plot): Extent = minExtent
  def render(plot: Plot, extent: Extent): Drawable = position match {
    case Position.Top    => f(extent).center(extent.width)
    case Position.Bottom => f(extent).center(extent.width)
    case Position.Left   => f(extent).middle(extent.height)
    case Position.Right  => f(extent).middle(extent.height)
    case _               => f(extent)
  }
}

object Label {
  def apply(position: Position, d: Drawable): Label = Label(position, _ => d, d.extent)
}

trait LabelImplicits {
  protected val plot: Plot

  val defaultTitleSize: Double = 22
  val defaultLabelSize: Double = 20

  def title(d: Drawable): Plot = plot :+ Label(Position.Top, d)
  def title(label: String, size: Double = defaultTitleSize): Plot =
    title(Text(label, size).padBottom(size / 2))

  def leftLabel(f: Extent => Drawable, width: Double): Plot = plot :+ Label(Position.Left, f, Extent(width, 0))
  def leftLabel(d: Drawable): Plot = plot :+ Label(Position.Left, _ => d, d.extent)
  def leftLabel(label: String, size: Double = defaultLabelSize): Plot =
    leftLabel(Text(label, size).rotated(270).padRight(size / 2))

  def rightLabel(f: Extent => Drawable, width: Double): Plot = plot :+ Label(Position.Right, f, Extent(width, 0))
  def rightLabel(d: Drawable): Plot = plot :+ Label(Position.Right, d)
  def rightLabel(label: String, size: Double = defaultLabelSize): Plot =
    rightLabel(Text(label, size).rotated(90).padLeft(size / 2))

  def topLabel(f: Extent => Drawable, height: Double): Plot = plot :+ Label(Position.Top, f, Extent(0, height))
  def topLabel(d: Drawable): Plot = plot :+ Label(Position.Top, d)
  def topLabel(label: String, size: Double = defaultLabelSize): Plot =
    topLabel(Text(label, size).padBottom(size / 2))

  def bottomLabel(f: Extent => Drawable, height: Double): Plot = plot :+ Label(Position.Bottom, f, Extent(0, height))
  def bottomLabel(d: Drawable): Plot = plot :+ Label(Position.Bottom, d)
  def bottomLabel(label: String, size: Double = defaultLabelSize): Plot =
    bottomLabel(Text(label, size).padTop(size / 2))

  def xLabel(d: Drawable): Plot = bottomLabel(d)
  def xLabel(label: String, size: Double = defaultLabelSize): Plot = bottomLabel(label, size)

  def yLabel(d: Drawable): Plot = leftLabel(d)
  def yLabel(label: String, size: Double = defaultLabelSize): Plot = leftLabel(label, size)
}
