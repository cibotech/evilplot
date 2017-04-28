package com.cibo.evilplot

import com.cibo.evilplot.colors.Color

package object geometry {

  implicit class Placeable(r: Renderable){
    def above(other: Renderable) = Above(r, other)
    def below(other: Renderable) = Above(other, r)
    def beside(other: Renderable) = Beside(r, other)
    def behind(other: Renderable) = Group(r, other)

    def labeled(msg: String) = Labeled(msg, r)
    def labeled(msgSize: (String, Double)) = Labeled(msgSize._1, r, msgSize._2)
    def titled(msg: String) = Titled(msg, r)
    def titled(msgSize: (String, Double)) = Titled(msgSize._1, r, msgSize._2)

    def padRight(pad: Double) = Pad(right = pad)(r)
    def padLeft(pad: Double) = Pad(left  = pad)(r)
    def padBottom(pad: Double) = Pad(bottom = pad)(r)
    def padTop(pad: Double) = Pad(top = pad)(r)
    def padAll(pad: Double) = Pad(pad)(r)

    def rotated(degress: Double) = Rotate(degress)(r)

    def filled(color: Color) = Style(fill = color)(r)

    // Experimental ... NO! These are silly.
    def -->(nudge: Double) = Translate(x = nudge)(r)
    def |^(nudge: Double) = Translate(y = nudge)(r)
    def -|(pad: Double) = Pad(right = pad)(r)
    def |-(pad: Double) = Pad(left  = pad)(r)
    def ⊥(pad: Double) = Pad(bottom = pad)(r)
    def ⊤(pad: Double) = Pad(top = pad)(r)

    // end Experimental
  }

  implicit class SeqPlaceable(sp: Seq[Renderable]){
    def distributeH: Renderable = DistributeH(sp)
    def distributeH(spacing: Double = 0): Renderable = DistributeH(sp, spacing)
    def distributeV: Renderable = DistributeV(sp)

    def group: Renderable = Group(sp :_*)
  }


  def FlowH(rs: Seq[Renderable], hasWidth: Extent): Renderable = {
    val consumed = rs.map(_.extent.width).sum
    val inBetween = (hasWidth.width - consumed) / (rs.length - 1)
    val padded = rs.init.map(_ padRight inBetween) :+ rs.last
    padded.reduce(Beside)
  }

  def DistributeH(rs: Seq[Renderable], spacing: Double = 0): Renderable =
    if(spacing == 0) rs.reduce(Beside)
    else {
      val padded = rs.init.map(_ padRight spacing) :+ rs.last
      padded.reduce(Beside)
    }

  def DistributeV(rs: Seq[Renderable], spacing: Double = 0): Renderable =
    if(spacing == 0) rs.reduce(Above)
    else {
      val padded = rs.init.map(_ padBottom spacing) :+ rs.last
      padded.reduce(Above)
    }
}
