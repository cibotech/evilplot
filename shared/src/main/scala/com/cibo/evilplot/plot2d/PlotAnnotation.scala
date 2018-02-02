package com.cibo.evilplot.plot2d

import com.cibo.evilplot.geometry._

// An annotation that is aligned with the data of a plot.
private[plot2d] abstract class PlotAnnotation[T] {

  // The position of this annotation.
  val position: PlotAnnotation.Position

  // Get the minimum size of this annotation.
  def size(plot: Plot2D[T]): Extent

  // Render the annotation for the plot.
  def render(plot: Plot2D[T], extent: Extent): Drawable
}

object PlotAnnotation {
  private[plot2d] sealed trait Position
  private[plot2d] case object Top extends Position
  private[plot2d] case object Bottom extends Position
  private[plot2d] case object Left extends Position
  private[plot2d] case object Right extends Position
  private[plot2d] case object Overlay extends Position

  private[plot2d] case class OverlayAnnotation[T](
    f: (Plot2D[T], Extent) => Drawable,
    x: Double,
    y: Double
  ) extends PlotAnnotation[T] {
    require(x >= 0.0 && x <= 1.0, s"x must be between 0.0 and 1.0, got $x")
    require(y >= 0.0 && y <= 1.0, s"y must be between 0.0 and 1.0, got $y")

    val position: Position = Overlay

    // Note that an overlay does not affect the size of the plot.
    def size(plot: Plot2D[T]): Extent = Extent(0, 0)

    def render(plot: Plot2D[T], extent: Extent): Drawable = {
      val drawable = f(plot, extent)
      val xoffset = (extent.width - drawable.extent.width) * x
      val yoffset = (extent.height - drawable.extent.height) * y
      Translate(drawable, x = xoffset, y = yoffset)
    }
  }

  implicit class PlotWithAnnotate[T](plot: Plot2D[T]) {

    /** Add an annotation to the plot.
      * @param f A function to create the drawable to render.
      * @param x The X coordinate to plot the drawable (between 0 to 1).
      * @param y The Y coordinate to plot the drawable (between 0 and 1).
      * @return The updated plot.
      */
    def annotate(f: (Plot2D[T], Extent) => Drawable, x: Double, y: Double): Plot2D[T] = {
      plot.copy(annotations = plot.annotations :+ OverlayAnnotation(f, x, y))
    }

    /** Add a text annotation to the plot.
      * @param msg The annotation.
      * @param x The X coordinate.
      * @param y The Y coordinate.
      * @return
      */
    def annotate(msg: String, x: Double = 1.0, y: Double = 0.5): Plot2D[T] =
      annotate((_, _) => msg.split('\n').map(Text(_)).reduce(above), x, y)
  }
}
