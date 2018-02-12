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
        border.xbounds(plot.xbounds).copy(xtransform = plot.xtransform).render(extent.copy(height = borderSize))
      case Position.Bottom =>
        val borderExent = extent.copy(height = borderSize)
        border.xbounds(plot.xbounds).copy(xtransform = plot.xtransform).render(borderExent).rotated(180).flipX
      case Position.Left   =>
        val borderExtent = Extent(extent.height, borderSize)
        border.xbounds(plot.ybounds).copy(xtransform = plot.ytransform).render(borderExtent).rotated(270)
      case Position.Right  =>
        val borderExtent = Extent(extent.height, borderSize)
        border.xbounds(plot.ybounds).copy(xtransform = plot.xtransform).render(borderExtent).rotated(90).flipY
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
