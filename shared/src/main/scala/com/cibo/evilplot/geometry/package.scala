/*
 * Copyright 2017 CiBO Technologies
 */

package com.cibo.evilplot

import com.cibo.evilplot.colors.Color

package object geometry {
  implicit class Placeable(r: Drawable) {
    def above(other: Drawable): Above = Above(r, other)
    def below(other: Drawable): Above = Above(other, r)
    def beside(other: Drawable): Beside = Beside(r, other)
    def behind(other: Drawable): Group = Group(Seq(r, other))
    def inFrontOf(other: Drawable): Group = Group(Seq(other, r))

    def labeled(msgSize: (String, Double)): Drawable = Align.center(r, Text(msgSize._1, msgSize._2) padTop 5).group
    def labeled(msg: String): Drawable = labeled(msg -> Text.defaultSize)

    def titled(msgSize: (String, Double)): Drawable = Pad(Text(msgSize._1, msgSize._2), bottom = msgSize._2 / 2.0)
    def titled(msg: String): Drawable = titled(msg -> Text.defaultSize)

    def padRight(pad: Double): Pad = Pad(r, right = pad)
    def padLeft(pad: Double): Pad = Pad(r, left  = pad)
    def padBottom(pad: Double): Pad = Pad(r, bottom = pad)
    def padTop(pad: Double): Pad = Pad(r, top = pad)
    def padAll(pad: Double): Pad = Pad(r, pad)

    def rotated(degrees: Double): Rotate = Rotate(r, degrees)

    def colored(color: Color): StrokeStyle = StrokeStyle(r, fill = color)
    def filled(color: Color): Style = Style(r, fill = color)
    def weighted(weight: Double): StrokeWeight = StrokeWeight(r, weight = weight)

    def transX(nudge: Double): Translate = Translate(r, x = nudge)
    def transY(nudge: Double): Translate = Translate(r, y = nudge)

    def affine(affine: AffineTransform): Affine = Affine(r, affine)
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
    padded.reduce(Beside.apply)
  }

  def distributeH(drawables: Seq[Drawable], spacing: Double = 0): Drawable = {
    require(drawables.nonEmpty, "distributeH must be called with a non-empty Seq[Drawable]")
    if (spacing == 0) drawables.reduce(Beside.apply)
    else {
      val padded = drawables.init.map(_ padRight spacing) :+ drawables.last
      padded.reduce(Beside.apply)
    }
  }

  def distributeV(drawables: Seq[Drawable], spacing: Double = 0): Drawable = {
    require(drawables.nonEmpty, "distributeV must be called with a non-empty Seq[Drawable]")
    if (spacing == 0) drawables.reduce(Above.apply)
    else {
      val padded = drawables.init.map(_ padBottom spacing) :+ drawables.last
      padded.reduce(Above.apply)
    }
  }

  def flipY(r: Drawable, height: Double): Drawable = FlipY(r, height)
  def flipY(r: Drawable): Drawable = flipY(r, r.extent.height)

  def flipX(r: Drawable, width: Double): Drawable = FlipX(r, width)
}
