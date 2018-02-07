package com.cibo.evilplot.plot.components

import com.cibo.evilplot.geometry.{Drawable, Extent}
import com.cibo.evilplot.plot.Plot

case class BorderPlot(
  position: Position,
  borderSize: Double,
  border: Plot[_]
) extends PlotComponent {
  override def size[T](plot: Plot[T]): Extent = Extent(borderSize, borderSize)
  def render[T](plot: Plot[T], extent: Extent): Drawable = {
    position match {
      case Position.Top    =>
        border.render(extent.copy(height = borderSize))
      case Position.Bottom =>
        border.render(extent.copy(height = borderSize)).rotated(180)
      case Position.Left   =>
        val borderExtent = Extent(extent.height, borderSize)
        border.render(borderExtent).rotated(270)
      case Position.Right  =>
        val borderExtent = Extent(extent.height, borderSize)
        border.render(borderExtent).rotated(90).flipY
      case _               =>
        border.render(extent)
    }
  }
}

trait BorderPlotImplicits[T] {
  protected val plot: Plot[T]

  val defaultSize: Double = 20

  def topPlot(p: Plot[_], size: Double = defaultSize): Plot[T] = plot :+ BorderPlot(Position.Top, size, p)
  def bottomPlot(p: Plot[_], size: Double = defaultSize): Plot[T] = plot :+ BorderPlot(Position.Bottom, size, p)
  def leftPlot(p: Plot[_], size: Double = defaultSize): Plot[T] = plot :+ BorderPlot(Position.Left, size, p)
  def rightPlot(p: Plot[_], size: Double = defaultSize): Plot[T] = plot :+ BorderPlot(Position.Right, size, p)
}
