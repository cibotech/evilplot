package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.{Drawable, Extent, Text}

object Label {

  private case class PlotLabel(
    position: PlotComponent.Position,
    label: Drawable
  ) extends PlotComponent {
    override def size[T](plot: Plot[T]): Extent = label.extent
    def render[T](plot: Plot[T], extent: Extent): Drawable = position match {
      case PlotComponent.Top    => label.center(extent.width)
      case PlotComponent.Bottom => label.center(extent.width)
      case PlotComponent.Left   => label.middle(extent.height)
      case PlotComponent.Right  => label.middle(extent.height)
      case _                    => label
    }
  }

  trait LabelImplicits[T] {
    protected val plot: Plot[T]

    val defaultTitleSize: Double = 22
    val defaultLabelSize: Double = 20

    def title(d: Drawable): Plot[T] = plot :+ PlotLabel(PlotComponent.Top, d)
    def title(label: String, size: Double = defaultTitleSize): Plot[T] =
      title(Text(label, size).padBottom(size / 2))

    def leftLabel(d: Drawable): Plot[T] = plot :+ PlotLabel(PlotComponent.Left, d)
    def leftLabel(label: String, size: Double = defaultLabelSize): Plot[T] =
      leftLabel(Text(label, size).rotated(270).padRight(size / 2))

    def rightLabel(d: Drawable): Plot[T] = plot :+ PlotLabel(PlotComponent.Right, d)
    def rightLabel(label: String, size: Double = defaultLabelSize): Plot[T] =
      rightLabel(Text(label, size).rotated(90).padLeft(size / 2))

    def topLabel(d: Drawable): Plot[T] = plot :+ PlotLabel(PlotComponent.Top, d)
    def topLabel(label: String, size: Double = defaultLabelSize): Plot[T] =
      topLabel(Text(label, size).padBottom(size / 2))

    def bottomLabel(d: Drawable): Plot[T] = plot :+ PlotLabel(PlotComponent.Bottom, d)
    def bottomLabel(label: String, size: Double = defaultLabelSize): Plot[T] =
      bottomLabel(Text(label, size).padTop(size / 2))

    def xLabel(d: Drawable): Plot[T] = bottomLabel(d)
    def xLabel(label: String, size: Double = defaultLabelSize): Plot[T] = bottomLabel(label, size)

    def yLabel(d: Drawable): Plot[T] = leftLabel(d)
    def yLabel(label: String, size: Double = defaultLabelSize): Plot[T] = leftLabel(label, size)
  }

}
