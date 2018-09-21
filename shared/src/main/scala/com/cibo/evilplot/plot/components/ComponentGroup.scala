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

import com.cibo.evilplot.geometry._
import com.cibo.evilplot.plot.Plot
import com.cibo.evilplot.plot.aesthetics.Theme

final case class ComponentGroup(
  position: Position,
  components: Seq[FacetedPlotComponent] = Seq.empty,
  override val repeated: Boolean = false
) extends FacetedPlotComponent {

  require(
    components.forall(_.position == position),
    "All components in a ComponentGroup must use the same Position")

  override def size(plot: Plot): Extent = {
    val (width, height) = components.foldLeft((0d, 0d)) {
      case ((maxWidth, maxHeight), component) =>
        val componentSize = component.size(plot)
        (math.max(maxWidth, componentSize.width), math.max(maxHeight, componentSize.height))
    }
    Extent(width, height)
  }

  def render(plot: Plot, extent: Extent, row: Int, column: Int)(implicit theme: Theme): Drawable = {
    if (components.isEmpty) {
      EmptyDrawable()
    } else {
      val rendered = components.map(_.render(plot, extent, row, column)(theme))
      position match {
        case Position.Top  => Align.bottomSeq(rendered).reduceLeft(_ inFrontOf _)
        case Position.Left => Align.rightSeq(rendered).reduceLeft(_ inFrontOf _)
        case _             => rendered.reduceLeft(_ inFrontOf _)
      }
    }
  }

}
