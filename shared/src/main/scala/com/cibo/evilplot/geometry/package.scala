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

package com.cibo.evilplot

import com.cibo.evilplot.colors.{Color, RGB}

package object geometry {
  implicit class Placeable(r: Drawable) {
    def above(other: Drawable): Drawable = geometry.above(r, other)
    def below(other: Drawable): Drawable = geometry.above(other, r)
    def beside(other: Drawable): Drawable = geometry.beside(r, other)
    def behind(other: Drawable): Drawable = Seq(r, other).group
    def inFrontOf(other: Drawable): Drawable = Seq(other, r).group

    def labeled(msgSize: (String, Double, String)): Drawable =
      Align.middle(r, Text(msgSize._1, msgSize._2, msgSize._3) padLeft 5).reduce(_ beside _)
    def labeled(msg: String): Drawable = labeled(msg, Text.defaultSize, Text.defaultFontFace)

    def titled(msgSize: (String, Double, String)): Drawable =
      Align
        .center(
          geometry.pad(Text(msgSize._1, msgSize._2, msgSize._3), bottom = msgSize._2 / 2.0),
          r)
        .reduce(_ above _)
    def titled(msg: String): Drawable = titled(msg, Text.defaultSize, Text.defaultFontFace)

    def padRight(pad: Double): Drawable = geometry.pad(r, right = pad)
    def padLeft(pad: Double): Drawable = geometry.pad(r, left = pad)
    def padBottom(pad: Double): Drawable = geometry.pad(r, bottom = pad)
    def padTop(pad: Double): Drawable = geometry.pad(r, top = pad)
    def padAll(pad: Double): Drawable = geometry.padAll(r, pad)

    def rotated(degrees: Double): Drawable = if (degrees == 0) r else Rotate(r, degrees)
    def scaled(x: Double = 1, y: Double = 1): Drawable = if (x == 1 && y == 1) r else Scale(r, x, y)

    def flipY(height: Double): Drawable =
      r.scaled(1, -1).translate(y = height).resize(r.extent.copy(height = height))
    def flipY: Drawable = r.scaled(1, -1).translate(y = r.extent.height)

    def flipX(width: Double): Drawable =
      r.scaled(-1, 1).translate(x = width).resize(r.extent.copy(width = width))
    def flipX: Drawable = r.scaled(-1, 1).translate(x = r.extent.width)

    def colored(color: Color): Drawable = StrokeStyle(r, fill = color)
    def filled(color: Color): Drawable = Style(r, fill = color)
    def filled(gradient: Gradient2d): Drawable = GradientFill(r, fill = gradient)
    def weighted(weight: Double): Drawable = StrokeWeight(r, weight = weight)
    def dashed(dashLength: Double): Drawable = LineDash(r, LineStyle.evenlySpaced(dashLength))
    def dashed(lineStyle: LineStyle): Drawable = LineDash(r, style = lineStyle)

    def transX(nudge: Double): Drawable = translate(x = nudge)
    def transY(nudge: Double): Drawable = translate(y = nudge)

    def resize(extent: Extent): Drawable = r match {
      case Resize(innerR, _) if extent == r.extent => r
      case Resize(innerR, _)                       => Resize(innerR, extent)
      case _ if extent == r.extent                 => r
      case _                                       => Resize(r, extent)
    }

    def affine(affine: AffineTransform): Drawable = Affine(r, affine)

    def center(width: Double = 0): Drawable = translate(x = (width - r.extent.width) / 2)
    def right(width: Double = 0): Drawable = translate(x = width - r.extent.width)
    def middle(height: Double = 0): Drawable = translate(y = (height - r.extent.height) / 2)
    def bottom(height: Double = 0): Drawable = translate(y = height - r.extent.height)

    /** Translate.
      * This will optimize away stacked translates.
      */
    def translate(x: Double = 0, y: Double = 0): Drawable = r match {
      case Translate(nextR, nextX, nextY) if nextX + x == 0 && nextY + y == 0 => nextR
      case Translate(nextR, nextX, nextY)                                     => Translate(nextR, nextX + x, nextY + y)
      case _ if x != 0 || y != 0                                              => Translate(r, x, y)
      case _                                                                  => r
    }

    // Draw a box around the drawable for debugging.
    def debug: Drawable = {
      val red = (math.random() * 255.0).toInt
      val green = (math.random() * 255.0).toInt
      val blue = (math.random() * 255.0).toInt
      Seq(StrokeStyle(BorderRect(r.extent.width, r.extent.height), RGB(red, green, blue)), r).group
    }
  }

  implicit class SeqPlaceable(drawables: Seq[Drawable]) {
    def seqDistributeH: Drawable = distributeH(drawables)
    def seqDistributeH(spacing: Double = 0): Drawable = distributeH(drawables, spacing)
    def seqDistributeV: Drawable = distributeV(drawables)
    def seqDistributeV(spacing: Double = 0): Drawable = distributeV(drawables, spacing)

    def group: Drawable = {
      // Flatten nested groups.
      val flattened = drawables.foldLeft(Vector.empty[Drawable]) { (ds, d) =>
        d match {
          case Group(inner)    => ds ++ inner
          case EmptyDrawable() => ds
          case _               => ds :+ d
        }
      }
      flattened.length match {
        case 0 => EmptyDrawable()
        case 1 => flattened.head
        case _ => Group(flattened)
      }
    }
  }

  def flowH(drawables: Seq[Drawable], hasWidth: Extent): Drawable = {
    val consumed = drawables.map(_.extent.width).sum
    val inBetween = (hasWidth.width - consumed) / (drawables.length - 1)
    val padded = drawables.init.map(_ padRight inBetween) :+ drawables.last
    padded.reduce(beside)
  }

  def distributeH(drawables: Seq[Drawable], spacing: Double = 0): Drawable = {
    require(drawables.nonEmpty, "distributeH must be called with a non-empty Seq[Drawable]")
    if (spacing == 0) drawables.reduce(beside)
    else {
      val padded = drawables.init.map(_ padRight spacing) :+ drawables.last
      padded.reduce(beside)
    }
  }

  def distributeV(drawables: Seq[Drawable], spacing: Double = 0): Drawable = {
    require(drawables.nonEmpty, "distributeV must be called with a non-empty Seq[Drawable]")
    if (spacing == 0) drawables.reduce(above)
    else {
      val padded = drawables.init.map(_ padBottom spacing) :+ drawables.last
      padded.reduce(above)
    }
  }

  def above(top: Drawable, bottom: Drawable): Drawable = {
    val newExtent = Extent(
      math.max(top.extent.width, bottom.extent.width),
      top.extent.height + bottom.extent.height
    )
    Seq(top, bottom.translate(y = top.extent.height)).group.resize(newExtent)
  }

  def beside(left: Drawable, right: Drawable): Drawable = {
    val newExtent = Extent(
      left.extent.width + right.extent.width,
      math.max(left.extent.height, right.extent.height)
    )
    Seq(left, right.translate(x = left.extent.width)).group.resize(newExtent)
  }

  def pad(
    item: Drawable,
    left: Double = 0,
    right: Double = 0,
    top: Double = 0,
    bottom: Double = 0): Drawable = {
    val newExtent = Extent(
      item.extent.width + left + right,
      item.extent.height + top + bottom
    )
    item.translate(x = left, y = top).resize(newExtent)
  }

  def padAll(item: Drawable, size: Double): Drawable = pad(item, size, size, size, size)

  def fit(item: Drawable, width: Double, height: Double): Drawable = {
    val oldExtent = item.extent
    val newAspectRatio = width / height
    val oldAspectRatio = oldExtent.width / oldExtent.height
    val widthIsLimiting = newAspectRatio < oldAspectRatio
    val (scale, padded) = if (widthIsLimiting) {
      val scale = width / oldExtent.width
      (scale, pad(item, top = ((height - oldExtent.height * scale) / 2) / scale))
    } else {
      val scale = height / oldExtent.height
      (scale, pad(item, left = ((width - oldExtent.width * scale) / 2) / scale))
    }
    padded.scaled(scale, scale)
  }
  def fit(item: Drawable, extent: Extent): Drawable = fit(item, extent.width, extent.height)
}
