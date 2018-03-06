package com.cibo.evilplot.plot.renderers

import com.cibo.evilplot.geometry._
import com.cibo.evilplot.plot.aesthetics.Theme

trait TickRenderer {
  def render(label: String): Drawable
}

object TickRenderer {

  val defaultTickThickness: Double = 1
  val defaultTickLength: Double = 5

  /** Create a renderer to render a tick on the x axis.
    *
    * @param length     The length of the tick line.
    * @param thickness  The thickness of the tick line.
    * @param rotateText The rotation of the label.
    */
  def xAxisTickRenderer(
    length: Double = defaultTickLength,
    thickness: Double = defaultTickThickness,
    rotateText: Double = 0
  )(implicit theme: Theme): TickRenderer = new TickRenderer {
    def render(label: String): Drawable = {
      val line = Line(length, thickness).rotated(90)
      Align.center(line, Style(Text(label.toString), theme.colors.tickLabel)
        .rotated(rotateText).padTop(2)).reduce(above)
    }
  }

  /** Create a renderer to render a tick on the y axis.
    *
    * @param length    The length of the tick line.
    * @param thickness The thickness of the tick line.
    */
  def yAxisTickRenderer(
    length: Double = defaultTickLength,
    thickness: Double = defaultTickThickness
  )(implicit theme: Theme): TickRenderer = new TickRenderer {
    def render(label: String): Drawable = {
      val line = Line(length, thickness)
      Align.middle(Style(Text(label.toString), theme.colors.tickLabel)
        .padRight(2).padBottom(2), line).reduce(beside)
    }
  }
}
