package com.cibo.evilplot.plot.components

import com.cibo.evilplot.geometry.{Drawable, Extent, Text, above}
import com.cibo.evilplot.plot.Plot
import com.cibo.evilplot.plot.aesthetics.Theme

case class Annotation(
  f: (Plot, Extent) => Drawable,
  x: Double,
  y: Double
) extends PlotComponent {
  require(x >= 0.0 && x <= 1.0, s"x must be between 0.0 and 1.0, got $x")
  require(y >= 0.0 && y <= 1.0, s"y must be between 0.0 and 1.0, got $y")
  val position: Position = Position.Overlay
  def render(plot: Plot, extent: Extent)(implicit theme: Theme): Drawable = {
    val drawable = f(plot, extent)
    val xoffset = (extent.width - drawable.extent.width) * x
    val yoffset = (extent.height - drawable.extent.height) * y
    drawable.translate(x = xoffset, y = yoffset)
  }
}


trait AnnotationImplicits {
  protected val plot: Plot

  /** Add an annotation to the plot.
    *
    * @param f A function to create the drawable to render.
    * @param x The X coordinate to plot the drawable (between 0 and 1).
    * @param y The Y coordinate to plot the drawable (between 0 and 1).
    * @return The updated plot.
    */
  def annotate(f: (Plot, Extent) => Drawable, x: Double, y: Double): Plot = {
    plot :+ Annotation(f, x, y)
  }

  /** Add a drawable annotation to the plot
    *
    * @param d The annotation.
    * @param x The X coordinate (between 0 and 1).
    * @param y The Y coordinate (between 0 and 1).
    */
  def annotate(d: Drawable, x: Double, y: Double): Plot = annotate((_, _) => d, x, y)

  /** Add a text annotation to the plot.
    *
    * @param msg The annotation.
    * @param x   The X coordinate (between 0 and 1).
    * @param y   The Y coordinate (between 0 and 1).
    */
  def annotate(
    msg: String,
    x: Double = 1.0,
    y: Double = 0.5
  )(implicit theme: Theme): Plot =
    annotate(msg.split('\n').map(s => Text(s, theme.fonts.annotationSize)).reduce(above), x, y)

}
