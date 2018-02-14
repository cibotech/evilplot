package com.cibo.evilplot.plot.components

import com.cibo.evilplot.colors.{Color, DefaultColors}
import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent, Rect, Text}
import com.cibo.evilplot.plot.Plot

case class FacetLabel(
  position: Position,
  labels: Seq[Drawable],
  backgroundColor: Color
) extends FacetedPlotComponent {
  override val repeated: Boolean = true
  override def size(plot: Plot): Extent = {
    val width = labels.maxBy(_.extent.width).extent.width
    val height = labels.maxBy(_.extent.height).extent.height
    Extent(width, height)
  }

  def render(plot: Plot, extent: Extent, row: Int, column: Int): Drawable = {
    val bg = Rect(extent) filled backgroundColor
    position match {
      case Position.Top | Position.Bottom => bg behind labels(column).center(extent.width)
      case Position.Right | Position.Left => bg behind labels(row).middle(extent.height)
      case _                              => throw new IllegalStateException(s"bad position: $position")
    }
  }
}

trait FacetLabelImplicits {
  protected val plot: Plot

  def topLabels(
    labels: Seq[String],
    backgroundColor: Color = DefaultColors.backgroundColor
  ): Plot = plot :+ FacetLabel(Position.Top, labels.map(Text(_).padBottom(4)), backgroundColor)

  def bottomLabels(
    labels: Seq[String],
    backgroundColor: Color = DefaultColors.backgroundColor
  ): Plot = plot :+ FacetLabel(Position.Bottom, labels.map(Text(_).padTop(4)), backgroundColor)

  def rightLabels(
    labels: Seq[String],
    backgroundColor: Color = DefaultColors.backgroundColor
  ): Plot = plot :+ FacetLabel(Position.Right, labels.map(Text(_).rotated(90).padLeft(4)), backgroundColor)

  def leftLabels(
    labels: Seq[String],
    backgroundColor: Color = DefaultColors.backgroundColor
  ): Plot = plot :+ FacetLabel(Position.Left, labels.map(Text(_).rotated(270).padRight(4)), backgroundColor)
}
