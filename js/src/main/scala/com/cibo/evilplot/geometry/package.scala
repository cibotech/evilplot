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
    def behind(other: Drawable): Group = Group(r, other)
    def inFrontOf(other: Drawable): Group = Group(other, r)

    def labeled(msg: String): Labeled = Labeled(msg, r)
    def labeled(msgSize: (String, Double)): Labeled = Labeled(msgSize._1, r, msgSize._2)
    def titled(msg: String): Titled = Titled(msg, r)
    def titled(msgSize: (String, Double)): Titled = Titled(msgSize._1, r, msgSize._2)

    def padRight(pad: Double): Pad = Pad(right = pad)(r)
    def padLeft(pad: Double): Pad = Pad(left  = pad)(r)
    def padBottom(pad: Double): Pad = Pad(bottom = pad)(r)
    def padTop(pad: Double): Pad = Pad(top = pad)(r)
    def padAll(pad: Double): Pad = Pad(pad)(r)

    def rotated(degrees: Double): Rotate = Rotate(degrees)(r)

    def colored(color: Color): StrokeStyle = StrokeStyle(fill = color)(r)
    def filled(color: Color): Style = Style(fill = color)(r)
    def weighted(weight: Double): StrokeWeight = StrokeWeight(weight = weight)(r)

    def transX(nudge: Double): Translate = Translate(x = nudge)(r)
    def transY(nudge: Double): Translate = Translate(y = nudge)(r)

    def affine(affine: AffineTransform): Affine = Affine(affine)(r)
  }

  implicit class SeqPlaceable(drawables: Seq[Drawable]) {
    def seqDistributeH: Drawable = distributeH(drawables)
    def seqDistributeH(spacing: Double = 0): Drawable = distributeH(drawables, spacing)
    def seqDistributeV: Drawable = distributeV(drawables)
    def seqDistributeV(spacing: Double = 0): Drawable = distributeV(drawables, spacing)

    def group: Drawable = Group(drawables: _*)
  }


  def flowH(drawables: Seq[Drawable], hasWidth: Extent): Drawable = {
    val consumed = drawables.map(_.extent.width).sum
    val inBetween = (hasWidth.width - consumed) / (drawables.length - 1)
    val padded = drawables.init.map(_ padRight inBetween) :+ drawables.last
    padded.reduce(Beside)
  }

  def distributeH(drawables: Seq[Drawable], spacing: Double = 0): Drawable = {
    require(drawables.nonEmpty, "distributeH must be called with a non-empty Seq[Drawable]")
    if (spacing == 0) drawables.reduce(Beside)
    else {
      val padded = drawables.init.map(_ padRight spacing) :+ drawables.last
      padded.reduce(Beside)
    }
  }

  def distributeV(drawables: Seq[Drawable], spacing: Double = 0): Drawable = {
    require(drawables.nonEmpty, "distributeV must be called with a non-empty Seq[Drawable]")
    if (spacing == 0) drawables.reduce(Above)
    else {
      val padded = drawables.init.map(_ padBottom spacing) :+ drawables.last
      padded.reduce(Above)
    }
  }
}
