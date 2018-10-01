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

import com.cibo.evilplot.colors.Color
import com.cibo.evilplot.geometry.{Drawable, Extent, Line, Rect, StrokeStyle}
import com.cibo.evilplot.plot.Plot
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.ExplicitImplicits

final case class Background(
  f: (Plot, Extent) => Drawable
) extends PlotComponent {
  val position: Position = Position.Background
  override val repeated: Boolean = true
  def render(plot: Plot, extent: Extent)(implicit theme: Theme): Drawable = f(plot, extent)
}

trait BackgroundImplicits extends ExplicitImplicits{
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

  /** Add a border frame around the plot. */
  def frame()(implicit theme: Theme): Plot =
    frame(theme.colors.frame, theme.elements.tickThickness)

  /** Add a border frame around the plot. */
  def frame(color: Color, strokeWidth: Double): Plot = {
    background(
      (_, e: Extent) =>
        StrokeStyle(
          Line(e.height, strokeWidth).rotated(90).center() above Line(e.width, strokeWidth)
            .middle(),
          color))
  }
}
