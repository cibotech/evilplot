package com.cibo.evilplot.plot.components

import com.cibo.evilplot.geometry.{Drawable, Extent, Text}
import com.cibo.evilplot.plot.Plot

case class Label(
  position: Position,
  label: Drawable
) extends PlotComponent {
  override def size(plot: Plot): Extent = label.extent
  def render(plot: Plot, extent: Extent): Drawable = position match {
    case Position.Top    => label.center(extent.width)
    case Position.Bottom => label.center(extent.width)
    case Position.Left   => label.middle(extent.height)
    case Position.Right  => label.middle(extent.height)
    case _               => label
  }
}

trait LabelImplicits {
  protected val plot: Plot

  val defaultTitleSize: Double = 22
  val defaultLabelSize: Double = 20

  def title(d: Drawable): Plot = plot :+ Label(Position.Top, d)
  def title(label: String, size: Double = defaultTitleSize): Plot =
    title(Text(label, size).padBottom(size / 2))

  def leftLabel(d: Drawable): Plot = plot :+ Label(Position.Left, d)
  def leftLabel(label: String, size: Double = defaultLabelSize): Plot =
    leftLabel(Text(label, size).rotated(270).padRight(size / 2))

  def rightLabel(d: Drawable): Plot = plot :+ Label(Position.Right, d)
  def rightLabel(label: String, size: Double = defaultLabelSize): Plot =
    rightLabel(Text(label, size).rotated(90).padLeft(size / 2))

  def topLabel(d: Drawable): Plot = plot :+ Label(Position.Top, d)
  def topLabel(label: String, size: Double = defaultLabelSize): Plot =
    topLabel(Text(label, size).padBottom(size / 2))

  def bottomLabel(d: Drawable): Plot = plot :+ Label(Position.Bottom, d)
  def bottomLabel(label: String, size: Double = defaultLabelSize): Plot =
    bottomLabel(Text(label, size).padTop(size / 2))

  def xLabel(d: Drawable): Plot = bottomLabel(d)
  def xLabel(label: String, size: Double = defaultLabelSize): Plot = bottomLabel(label, size)

  def yLabel(d: Drawable): Plot = leftLabel(d)
  def yLabel(label: String, size: Double = defaultLabelSize): Plot = leftLabel(label, size)
}
