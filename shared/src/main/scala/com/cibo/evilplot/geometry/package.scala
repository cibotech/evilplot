/*
 * Copyright 2017 CiBO Technologies
 */

package com.cibo.evilplot

import com.cibo.evilplot.colors.{Color, RGB}

package object geometry {
  implicit class Placeable(r: Drawable) {
    def above(other: Drawable): Drawable = geometry.above(r, other)
    def below(other: Drawable): Drawable = geometry.above(other, r)
    def beside(other: Drawable): Drawable = geometry.beside(r, other)
    def behind(other: Drawable): Drawable = Group(Seq(r, other))
    def inFrontOf(other: Drawable): Drawable = Group(Seq(other, r))

    def labeled(msgSize: (String, Double)): Drawable = Align.center(r, Text(msgSize._1, msgSize._2) padTop 5).group
    def labeled(msg: String): Drawable = labeled(msg -> Text.defaultSize)

    def titled(msgSize: (String, Double)): Drawable = geometry.pad(Text(msgSize._1, msgSize._2), bottom = msgSize._2 / 2.0)
    def titled(msg: String): Drawable = titled(msg -> Text.defaultSize)

    def padRight(pad: Double): Drawable = geometry.pad(r, right = pad)
    def padLeft(pad: Double): Drawable = geometry.pad(r, left  = pad)
    def padBottom(pad: Double): Drawable = geometry.pad(r, bottom = pad)
    def padTop(pad: Double): Drawable = geometry.pad(r, top = pad)
    def padAll(pad: Double): Drawable = geometry.padAll(r, pad)

    def rotated(degrees: Double): Drawable = Rotate(r, degrees)
    def scaled(x: Double = 1, y: Double = 1): Drawable = Scale(r, x, y)

    def colored(color: Color): Drawable = StrokeStyle(r, fill = color)
    def filled(color: Color): Drawable = Style(r, fill = color)
    def weighted(weight: Double): Drawable = StrokeWeight(r, weight = weight)

    def transX(nudge: Double): Drawable = translate(x = nudge)
    def transY(nudge: Double): Drawable = translate(y = nudge)

    def affine(affine: AffineTransform): Affine = Affine(r, affine)

    def center(width: Double): Drawable = translate(x = (width - r.extent.width) / 2)
    def right(width: Double): Drawable = translate(x = width - r.extent.width)
    def middle(height: Double): Drawable = translate(y = (height - r.extent.height) / 2)
    def bottom(height: Double): Drawable = translate(y = height - r.extent.height)

    /** Translate.
      * This will optimize away stacked translates.
      */
    def translate(x: Double = 0, y: Double = 0): Drawable = r match {
      case Translate(nextR, nextX, nextY) if nextX + x == 0 && nextY + y == 0 => nextR
      case Translate(nextR, nextX, nextY)                                     => Translate(nextR, nextX + x, nextY + y)
      case _                                                                  => Translate(r, x, y)
    }

    // Draw a box around the drawable for debugging.
    def debug: Drawable = {
      val red = (math.random * 255.0).toInt
      val green = (math.random * 255.0).toInt
      val blue = (math.random * 255.0).toInt
      Group(Seq(StrokeStyle(BorderRect(r.extent.width, r.extent.height), RGB(red, green, blue)), r))
    }
  }

  implicit class SeqPlaceable(drawables: Seq[Drawable]) {
    def seqDistributeH: Drawable = distributeH(drawables)
    def seqDistributeH(spacing: Double = 0): Drawable = distributeH(drawables, spacing)
    def seqDistributeV: Drawable = distributeV(drawables)
    def seqDistributeV(spacing: Double = 0): Drawable = distributeV(drawables, spacing)

    def group: Drawable = Group(drawables)
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
    Resize(Group(Seq(top, bottom.translate(y = top.extent.height))), newExtent)
  }

  def beside(left: Drawable, right: Drawable): Drawable = {
    val newExtent = Extent(
      left.extent.width + right.extent.width,
      math.max(left.extent.height, right.extent.height)
    )
    Resize(Group(Seq(left, right.translate(x = left.extent.width))), newExtent)
  }

  def flipY(r: Drawable, height: Double): Drawable =
    Resize(Scale(r, 1, -1).translate(y = height), r.extent.copy(height = height))
  def flipY(r: Drawable): Drawable = Translate(Scale(r, 1, -1), y = r.extent.height)

  def flipX(r: Drawable, width: Double): Drawable =
    Resize(Scale(r, -1, 1).translate(x = width), r.extent.copy(width = width))

  def pad(item: Drawable, left: Double = 0, right: Double = 0, top: Double = 0, bottom: Double = 0): Drawable = {
    val newExtent = Extent(
      item.extent.width + left + right,
      item.extent.height + top + bottom
    )
    Resize(item.translate(x = left, y = top), newExtent)
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
    Scale(padded, scale, scale)
  }
  def fit(item: Drawable, extent: Extent): Drawable = fit(item, extent.width, extent.height)
}
