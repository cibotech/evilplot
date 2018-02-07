package com.cibo.evilplot.plot.components

import com.cibo.evilplot.colors.{Color, DefaultColors}
import com.cibo.evilplot.geometry.{Drawable, Extent, Rect}
import com.cibo.evilplot.plot.Plot

case class Background(
  f: (Plot[_], Extent) => Drawable
) extends PlotComponent {
  val position: Position = Position.Background
  override val repeated: Boolean = true
  def render[T](plot: Plot[T], extent: Extent): Drawable = f(plot, extent)
}

trait BackgroundImplicits[T] {
  protected val plot: Plot[T]

  /** Set the background (this will replace any existing background).
    * @param f Function to render the background.
    */
  def background(f: (Plot[_], Extent) => Drawable): Plot[T] = {
    // Place the background on the bottom so that it goes under grid lines, etc.
    val bg = Background(f)
    bg +: plot.copy(components = plot.components.filterNot(_.isInstanceOf[Background]))
  }

  /** Add a solid background.
    * @param color The background color
    */
  def background(color: Color = DefaultColors.backgroundColor): Plot[T] =
    background((_, e) => Rect(e).filled(color))
}
