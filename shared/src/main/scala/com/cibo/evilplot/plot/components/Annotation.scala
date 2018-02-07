package com.cibo.evilplot.plot.components

import com.cibo.evilplot.geometry.{Drawable, Extent, Text, above}
import com.cibo.evilplot.plot.Plot

case class Annotation(
  f: (Plot[_], Extent) => Drawable,
  x: Double,
  y: Double
) extends PlotComponent {
  require(x >= 0.0 && x <= 1.0, s"x must be between 0.0 and 1.0, got $x")
  require(y >= 0.0 && y <= 1.0, s"y must be between 0.0 and 1.0, got $y")
  val position: Position = Position.Overlay
  def render[T](plot: Plot[T], extent: Extent): Drawable = {
    val drawable = f(plot, extent)
    val xoffset = (extent.width - drawable.extent.width) * x
    val yoffset = (extent.height - drawable.extent.height) * y
    drawable.translate(x = xoffset, y = yoffset)
  }
}


trait AnnotationImplicits[T] {
  protected val plot: Plot[T]

  /** Add an annotation to the plot.
    *
    * @param f A function to create the drawable to render.
    * @param x The X coordinate to plot the drawable (between 0 to 1).
    * @param y The Y coordinate to plot the drawable (between 0 and 1).
    * @return The updated plot.
    */
  def annotate(f: (Plot[_], Extent) => Drawable, x: Double, y: Double): Plot[T] = {
    plot :+ Annotation(f, x, y)
  }

  /** Add a text annotation to the plot.
    *
    * @param msg The annotation.
    * @param x   The X coordinate.
    * @param y   The Y coordinate.
    * @return
    */
  def annotate(msg: String, x: Double = 1.0, y: Double = 0.5): Plot[T] =
    annotate((_, _) => msg.split('\n').map(Text(_)).reduce(above), x, y)

}
