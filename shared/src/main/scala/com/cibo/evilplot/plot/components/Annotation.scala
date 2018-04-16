/*
 * Copyright (c) 2018, CiBO Technologies, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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
    annotate(msg.split('\n').map(s => Text(s, theme.fonts.annotationSize, theme.fonts.fontFace)).reduce(above), x, y)

}
