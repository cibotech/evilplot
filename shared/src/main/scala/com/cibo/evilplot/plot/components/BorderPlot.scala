package com.cibo.evilplot.plot.components

import com.cibo.evilplot.geometry.{Drawable, Extent}
import com.cibo.evilplot.plot.Plot

case class BorderPlot(
  position: Position,
  borderSize: Double,
  border: Plot
) extends PlotComponent {
  override def size(plot: Plot): Extent = Extent(borderSize, borderSize)
  def render(plot: Plot, extent: Extent): Drawable = {
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

trait BorderPlotImplicits {
  protected val plot: Plot

  val defaultSize: Double = 20

  def topPlot(p: Plot, size: Double = defaultSize): Plot = plot :+ BorderPlot(Position.Top, size, p)
  def bottomPlot(p: Plot, size: Double = defaultSize): Plot = plot :+ BorderPlot(Position.Bottom, size, p)
  def leftPlot(p: Plot, size: Double = defaultSize): Plot = plot :+ BorderPlot(Position.Left, size, p)
  def rightPlot(p: Plot, size: Double = defaultSize): Plot = plot :+ BorderPlot(Position.Right, size, p)
}
