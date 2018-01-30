package com.cibo.evilplot.geometry

object TextMetrics extends TextMetricsInterface {
  def measure(text: Text): Extent = {
    val canvas = new java.awt.Canvas()
    val graphics = canvas.getGraphics
    val font = graphics.getFont.deriveFont(text.size.toFloat)
    val metrics = graphics.getFontMetrics(font)
    val width = metrics.stringWidth(text.msg)
    val height = metrics.getHeight
    Extent(width, height)
  }
}
