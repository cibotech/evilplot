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

import com.cibo.evilplot.geometry.{Drawable, Extent}
import com.cibo.evilplot.plot.Plot
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.ExplicitImplicits

/** A component that is aligned with the data of a plot. */
trait FacetedPlotComponent extends ExplicitImplicits{

  /** The position of this component. */
  val position: Position

  /** Determine if this component is repeated in facets.
    * For example, axes and backgrounds are repeated.
    */
  val repeated: Boolean = false

  /** Get the minimum size of this component. */
  def size(plot: Plot): Extent = Extent(0, 0)

  /** Render the component for a particular facet.
    * @param plot The plot or subplot if a facet.
    * @param extent The extent this component gets.
    * @param row The facet row (or 0).
    * @param column The facet column (or 0).
    */
  def render(plot: Plot, extent: Extent, row: Int, column: Int)(implicit theme: Theme): Drawable
}
