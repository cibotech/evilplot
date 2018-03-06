package com.cibo.evilplot.plot.components

import com.cibo.evilplot.colors.Color
import com.cibo.evilplot.geometry.{Drawable, Extent, Rect}
import com.cibo.evilplot.plot.Plot
import com.cibo.evilplot.plot.aesthetics.Theme

case class Background(
  f: (Plot, Extent) => Drawable
) extends PlotComponent {
  val position: Position = Position.Background
  override val repeated: Boolean = true
  def render(plot: Plot, extent: Extent)(implicit theme: Theme): Drawable = f(plot, extent)
}

trait BackgroundImplicits {
  protected val plot: Plot

  /** Set the background (this will replace any existing background).
    * @param f Function to render the background.
    */
  def background(f: (Plot, Extent) => Drawable): Plot = {
    // Place the background on the bottom so that it goes under grid lines, etc.
    val bg = Background(f)
    bg +: plot.copy(components = plot.components.filterNot(_.isInstanceOf[Background]))
  }

  /** Add a solid background.
    * @param color The background color
    */
  def background(color: Color): Plot =
    background((_, e) => Rect(e).filled(color))

  /** Add a solid background. */
  def background()(implicit theme: Theme): Plot =
    background((_, e) => Rect(e).filled(theme.colors.background))
}
