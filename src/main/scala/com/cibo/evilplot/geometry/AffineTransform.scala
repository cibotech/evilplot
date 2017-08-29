package com.cibo.evilplot.geometry

final case class AffineTransform(scaleX: Double = 1,
                                 shearX: Double = 0,
                                 shearY: Double = 0,
                                 scaleY: Double = 1,
                                 shiftX: Double = 0,
                                 shiftY: Double = 0) {

  def scale(x: Double = 1, y: Double = 1): AffineTransform =
    copy(scaleX = x * scaleX, scaleY = y * scaleY)

  def shift(dx: Double, dy: Double): AffineTransform =
    copy(shiftX = dx + shiftX, shiftY = dy + shiftY)

  /** Rotates the transformation by `theta` radians.
    * "Unsafe" because this can cause issues with bounding boxes if `theta` is not a multiple of pi/2.
    */
  def unsafeRotate(theta: Double): AffineTransform = {

    /*

      [ cos(theta)  -sin(theta  0         [ scaleX  shearX  shiftX
        sin(theta)  cos(theta)  0     x     shearY  scaleY  shiftY      =   ???
        0           0           1 ]         0       0       1      ]

     */

    ???

  }

  /** Rotates the transformation by `theta` degrees.
    * "Unsafe" because this can cause issues with bounding boxes if `theta` is not a multiple of pi/2.
    */
  def unsafeRotateDegrees(degs: Double): AffineTransform =
    unsafeRotate(degs / 180 * Math.PI)

  def flipOverX: AffineTransform = scale(y = -1)

  def flipOverY: AffineTransform = scale(x = -1)

  def rotateClockwise: AffineTransform =
    unsafeRotateDegrees(-90)

  def rotateCounterClockwise: AffineTransform =
    unsafeRotateDegrees(90)

}

object AffineTransform {

  def identity: AffineTransform = AffineTransform()

}
