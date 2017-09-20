/*
 * Copyright 2017 CiBO Technologies
 */

package com.cibo.evilplot.geometry

case class AffineTransform(scaleX: Double = 1,
    shearX: Double = 0,
    shearY: Double = 0,
    scaleY: Double = 1,
    shiftX: Double = 0,
    shiftY: Double = 0) {

  def scale(x: Double = 1, y: Double = 1): AffineTransform = this.compose(AffineTransform.scale(x, y))

  def translate(dx: Double, dy: Double): AffineTransform = this.compose(AffineTransform.translate(dx, dy))

  /** Rotates the transformation by `theta` radians.  */
  def rotate(theta: Double): AffineTransform = this.compose(AffineTransform.rotate(theta))

  /** Rotates the transformation by `theta` degrees. */
  def rotateDegrees(degs: Double): AffineTransform = rotate(degs / 180 * Math.PI)

  def flipOverX: AffineTransform = scale(y = -1)

  def flipOverY: AffineTransform = scale(x = -1)

  def rotateClockwise: AffineTransform = rotateDegrees(-90)

  def rotateCounterClockwise: AffineTransform = rotateDegrees(90)

  def compose(trans: AffineTransform): AffineTransform = {
    val newScaleX = trans.scaleX * scaleX + trans.shearX * shearY
    val newShearX = trans.scaleX * shearX + trans.shearX * scaleY
    val newShiftX = trans.scaleX * shiftX + trans.shearX * shiftY + trans.shiftX
    val newShearY = trans.shearY * scaleX + trans.scaleY * shearY
    val newScaleY = trans.shearY * shearX + trans.scaleY * scaleY
    val newShiftY = trans.shearY * shiftX + trans.scaleY * shiftY + trans.shiftY

    AffineTransform(newScaleX, newShearX, newShearY, newScaleY, newShiftX, newShiftY)
  }

  def apply(x: Double, y: Double): (Double, Double) = {
    val tx = x * scaleX + y * shearX + shiftX
    val ty = x * shearY + y * scaleY + shiftY
    (tx, ty)
  }
}

object AffineTransform {

  def identity: AffineTransform = AffineTransform()

  private def scale(sx: Double, sy: Double): AffineTransform = AffineTransform(scaleX = sx, scaleY = sy)

  private def translate(dx: Double, dy: Double): AffineTransform = AffineTransform(shiftX = dx, shiftY = dy)

  private def rotate(theta: Double): AffineTransform =
    AffineTransform(scaleX = Math.cos(theta), shearX = -Math.sin(theta),
                    shearY = Math.sin(theta), scaleY = Math.cos(theta) )
}
