/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.colors

// These are the default ggplot colors...maybe we could be more creative.
object DefaultColors {
  val backgroundColor = HSL(0, 0, 92)
  val barColor = HSL(0, 0, 35)
  val titleBarColor = HSL(0, 0, 85)

  val nicePalette = Seq(
    RGB(26, 188, 156),
    RGB(46, 204, 113),
    RGB(52, 152, 219),
    RGB(155, 89, 182),
    RGB(52, 73, 94),
    RGB(241, 196, 15),
    RGB(230, 126, 34),
    RGB(231, 76, 60),
    // darker vv
    RGB(22, 160, 133),
    RGB(39, 174, 96),
    RGB(41, 128, 185),
    RGB(142, 68, 173),
    RGB(44, 62, 80),
    RGB(243, 156, 18),
    RGB(211, 84, 0),
    RGB(192, 57, 43)
  )
}
