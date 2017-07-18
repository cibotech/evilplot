package com.cibo.evilplot

import com.cibo.evilplot.colors.Color

package object geometry {

  implicit class Placeable(r: Drawable) {
    def above(other: Drawable): Above = Above(r, other)
    def below(other: Drawable): Above = Above(other, r)
    def beside(other: Drawable): Beside = Beside(r, other)
    def behind(other: Drawable): Group = Group(r, other)

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

    def transX(nudge: Double): Translate = Translate(x = nudge)(r)
    def transY(nudge: Double): Translate = Translate(y = nudge)(r)
  }

  implicit class SeqPlaceable(sp: Seq[Drawable]) {
    def seqDistributeH: Drawable = distributeH(sp)
    def seqDistributeH(spacing: Double = 0): Drawable = distributeH(sp, spacing)
    def seqDistributeV: Drawable = distributeV(sp)

    def group: Drawable = Group(sp: _*)
  }


  def flowH(rs: Seq[Drawable], hasWidth: Extent): Drawable = {
    val consumed = rs.map(_.extent.width).sum
    val inBetween = (hasWidth.width - consumed) / (rs.length - 1)
    val padded = rs.init.map(_ padRight inBetween) :+ rs.last
    padded.reduce(Beside)
  }

  def distributeH(rs: Seq[Drawable], spacing: Double = 0): Drawable = {
    require(rs.nonEmpty, "distributeH must be called with a non-empty Seq[Drawable]")
    if (spacing == 0) rs.reduce(Beside)
    else {
      val padded = rs.init.map(_ padRight spacing) :+ rs.last
      padded.reduce(Beside)
    }
  }

  def distributeV(rs: Seq[Drawable], spacing: Double = 0): Drawable = {
    require(rs.nonEmpty, "distributeV must be called with a non-empty Seq[Drawable]")
    if (spacing == 0) rs.reduce(Above)
    else {
      val padded = rs.init.map(_ padBottom spacing) :+ rs.last
      padded.reduce(Above)
    }
  }
}
