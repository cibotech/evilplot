package com.cibo.evilplot.colors

import com.cibo.evilplot.geometry.GradientStop

object ColorGradients {

  val spectral: Seq[HSLA] = Seq(RGB(215, 25, 28), RGB(255,255,191), RGB(43,131,186))

  val viridis: Seq[HSLA] = Seq(RGB(68,1,84), RGB(71,44,122), RGB(59,81,139), RGB(44,113,142),
    RGB(33,144,141), RGB(39,173,129), RGB(92,200,99), RGB(170,220,50), RGB(253,231,37))

  val inferno: Seq[HSLA] = Seq(RGB(0,0,4), RGB(31,12,72), RGB(85,15,109), RGB(136,34,106),
    RGB(186,54,85), RGB(227,89,51), RGB(249,140,10), RGB(249,201,50), RGB(252,255,164))

  val magma: Seq[HSLA] = Seq(RGB(0,0,4), RGB(28,16,68), RGB(79,18,123), RGB(129,37,129),
    RGB(181,54,122), RGB(229,80,100), RGB(251,135,97), RGB(254,194,135), RGB(252,253,191))

  val plasma: Seq[HSLA] = Seq(RGB(13,8,135), RGB(75,3,161), RGB(125,3,168), RGB(168,34,150),
    RGB(203,70,121), RGB(229,107,93), RGB(248,148,65), RGB(253,195,40), RGB(240,249,33))

}

object FillGradients {

  def distributeEvenly(seq: Seq[Color]): Seq[GradientStop] = {
    if(seq.length > 1) {
      (0 until seq.length - 1).map { idx =>
        GradientStop(idx / (seq.length - 1).toDouble, seq(idx))
      } :+ GradientStop(1.0, seq.last)
    } else if(seq.length == 1) {
      Seq(GradientStop(0.0, seq.head), GradientStop(1.0, seq.head))
    } else Seq()
  }

  val spectral: Seq[GradientStop] = distributeEvenly(ColorGradients.spectral)
  val viridis: Seq[GradientStop] = distributeEvenly(ColorGradients.viridis)
  val inferno: Seq[GradientStop] = distributeEvenly(ColorGradients.inferno)
  val magma: Seq[GradientStop] = distributeEvenly(ColorGradients.magma)
  val plasma: Seq[GradientStop] = distributeEvenly(ColorGradients.plasma)

}
