package com.cibo.evilplot.geometry

import java.awt.BasicStroke

import com.cibo.evilplot.colors.{Clear, Color, ColorUtils, HSLA}

private[geometry] trait Graphics2DSupport {
  implicit class ColorConverters(c: Color) {
    def asJava: java.awt.Color = c match {
      case hsla: HSLA =>
        val (r, g, b, a) = ColorUtils.hslaToRgba(hsla)
        new java.awt.Color(r.toFloat, g.toFloat, b.toFloat, a.toFloat)
      case Clear => new java.awt.Color(0.0f, 0.0f, 0.0f, 0.0f)
    }
  }

  implicit class TransformConverters(affine: AffineTransform) {
    def asJava: java.awt.geom.AffineTransform = {
      new java.awt.geom.AffineTransform(affine.scaleX, affine.shearY,
        affine.shearX, affine.scaleY, affine.shiftX, affine.shiftY)
    }
  }

  implicit class StrokeWeightConverters(strokeWeight: Double) {
    def asStroke: java.awt.Stroke = new BasicStroke(strokeWeight.toFloat)
  }
}
