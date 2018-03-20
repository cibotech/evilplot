package com.cibo.evilplot.plot.components

import com.cibo.evilplot.colors.HTMLNamedColors
import com.cibo.evilplot.geometry.{Drawable, Extent, StrokeStyle, Style, Text}
import com.cibo.evilplot.plot.aesthetics.Theme
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
  def render(plot: Plot, extent: Extent)(implicit theme: Theme): Drawable = position match {
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

  def title(d: Drawable): Plot = plot :+ Label(Position.Top, d)
  def title(label: String)(implicit theme: Theme): Plot = {
    title(Style(Text(label, theme.fonts.titleSize), theme.colors.title).padBottom(theme.fonts.titleSize / 2))
  }

  def leftLabel(f: Extent => Drawable, width: Double): Plot = plot :+ Label(Position.Left, f, Extent(width, 0))
  def leftLabel(d: Drawable): Plot = plot :+ Label(Position.Left, _ => d, d.extent)
  def leftLabel(label: String)(implicit theme: Theme): Plot =
    leftLabel(Style(Text(label, theme.fonts.labelSize), theme.colors.label).rotated(270).padRight(theme.fonts.labelSize / 2))

  def rightLabel(f: Extent => Drawable, width: Double): Plot = plot :+ Label(Position.Right, f, Extent(width, 0))
  def rightLabel(d: Drawable): Plot = plot :+ Label(Position.Right, d)
  def rightLabel(label: String)(implicit theme: Theme): Plot =
    rightLabel(Style(Text(label, theme.fonts.labelSize), theme.colors.label).rotated(90).padLeft(theme.fonts.labelSize / 2))

  def topLabel(f: Extent => Drawable, height: Double): Plot = plot :+ Label(Position.Top, f, Extent(0, height))
  def topLabel(d: Drawable): Plot = plot :+ Label(Position.Top, d)
  def topLabel(label: String)(implicit theme: Theme): Plot =
    topLabel(Style(Text(label, theme.fonts.labelSize), theme.colors.label).padBottom(theme.fonts.labelSize / 2))

  def bottomLabel(f: Extent => Drawable, height: Double): Plot = plot :+ Label(Position.Bottom, f, Extent(0, height))
  def bottomLabel(d: Drawable): Plot = plot :+ Label(Position.Bottom, d)
  def bottomLabel(label: String)(implicit theme: Theme): Plot =
    bottomLabel(Style(Text(label, theme.fonts.labelSize), theme.colors.label).padTop(theme.fonts.labelSize / 2))

  def xLabel(d: Drawable): Plot = bottomLabel(d)
  def xLabel(label: String)(implicit theme: Theme): Plot = bottomLabel(label)

  def yLabel(d: Drawable): Plot = leftLabel(d)
  def yLabel(label: String)(implicit theme: Theme): Plot = leftLabel(label)
}
