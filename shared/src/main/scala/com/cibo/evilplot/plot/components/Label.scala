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

import com.cibo.evilplot.colors.{Color, HTMLNamedColors}
import com.cibo.evilplot.geometry.{Drawable, Extent, StrokeStyle, Style, Text}
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.Plot

/** A plot label.
  * @param position The position of this component.
  * @param f A function to create the label for the given extent.
  * @param minExtent The minimum extent.
  */
case class Label(
  position: Position,
  f: Extent => Drawable,
  minExtent: Extent
) extends PlotComponent {
  override def size(plot: Plot): Extent = minExtent
  def render(plot: Plot, extent: Extent)(implicit theme: Theme): Drawable = position match {
    case Position.Top    => f(extent).center(extent.width)
    case Position.Bottom => f(extent).center(extent.width)
    case Position.Left   => f(extent).middle(extent.height)
    case Position.Right  => f(extent).middle(extent.height)
    case _               => f(extent)
  }
}

object Label {
  def apply(position: Position, d: Drawable): Label = Label(position, _ => d, d.extent)
}

trait LabelImplicits {
  protected val plot: Plot

  def title(d: Drawable): Plot = plot :+ Label(Position.Top, d)
  def title(
    label: String,
    size: Option[Double] = None,
    color: Option[Color] = None
  )(implicit theme: Theme): Plot = {
    val titleSize = size.getOrElse(theme.fonts.titleSize)
    val titleColor = color.getOrElse(theme.colors.title)
    title(Style(Text(label, titleSize, theme.fonts.fontFace), titleColor).padBottom(titleSize / 2))
  }

  def leftLabel(f: Extent => Drawable, width: Double): Plot = plot :+ Label(Position.Left, f, Extent(width, 0))
  def leftLabel(d: Drawable): Plot = plot :+ Label(Position.Left, _ => d, d.extent)
  def leftLabel(
    label: String,
    size: Option[Double] = None,
    color: Option[Color] = None
  )(implicit theme: Theme): Plot = {
    val labelSize = size.getOrElse(theme.fonts.labelSize)
    val labelColor = color.getOrElse(theme.colors.label)
    leftLabel(Style(Text(label, labelSize, theme.fonts.fontFace), labelColor).rotated(270).padRight(labelSize / 2))
  }

  def rightLabel(f: Extent => Drawable, width: Double): Plot = plot :+ Label(Position.Right, f, Extent(width, 0))
  def rightLabel(d: Drawable): Plot = plot :+ Label(Position.Right, d)
  def rightLabel(
    label: String,
    size: Option[Double] = None,
    color: Option[Color] = None
  )(implicit theme: Theme): Plot = {
    val labelSize = size.getOrElse(theme.fonts.labelSize)
    val labelColor = color.getOrElse(theme.colors.label)
    rightLabel(Style(Text(label, labelSize, theme.fonts.fontFace), labelColor).rotated(90).padLeft(labelSize / 2))
  }

  def topLabel(f: Extent => Drawable, height: Double): Plot = plot :+ Label(Position.Top, f, Extent(0, height))
  def topLabel(d: Drawable): Plot = plot :+ Label(Position.Top, d)
  def topLabel(
    label: String,
    size: Option[Double] = None,
    color: Option[Color] = None
  )(implicit theme: Theme): Plot = {
    val labelSize = size.getOrElse(theme.fonts.labelSize)
    val labelColor = color.getOrElse(theme.colors.label)
    topLabel(Style(Text(label, labelSize, theme.fonts.fontFace), labelColor).padBottom(labelSize / 2))
  }

  def bottomLabel(f: Extent => Drawable, height: Double): Plot = plot :+ Label(Position.Bottom, f, Extent(0, height))
  def bottomLabel(d: Drawable): Plot = plot :+ Label(Position.Bottom, d)
  def bottomLabel(
    label: String,
    size: Option[Double] = None,
    color: Option[Color] = None
  )(implicit theme: Theme): Plot = {
    val labelSize = size.getOrElse(theme.fonts.labelSize)
    val labelColor = color.getOrElse(theme.colors.label)
    bottomLabel(Style(Text(label, labelSize, theme.fonts.fontFace), labelColor).padTop(labelSize / 2))
  }

  def xLabel(d: Drawable): Plot = bottomLabel(d)
  def xLabel(
    label: String,
    size: Option[Double] = None,
    color: Option[Color] = None
  )(implicit theme: Theme): Plot = bottomLabel(label, size, color)

  def yLabel(d: Drawable): Plot = leftLabel(d)
  def yLabel(
    label: String,
    size: Option[Double] = None,
    color: Option[Color] = None
  )(implicit theme: Theme): Plot = leftLabel(label, size, color)
}
