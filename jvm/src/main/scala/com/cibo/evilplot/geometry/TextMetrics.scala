package com.cibo.evilplot.geometry

import java.awt.font.FontRenderContext
import java.awt.Font

object TextMetrics extends TextMetricsInterface {

  private lazy val transform = new java.awt.geom.AffineTransform()
  private lazy val frc = new FontRenderContext(transform, true, true)
  private lazy val font = Font.decode(Font.SANS_SERIF)

  def measure(text: Text): Extent = {
    val fontWithSize = font.deriveFont(text.size.toFloat)
    val width = fontWithSize.getStringBounds(text.msg, frc).getWidth
    val height = fontWithSize.getSize2D
    Extent(width, height)
  }
}
