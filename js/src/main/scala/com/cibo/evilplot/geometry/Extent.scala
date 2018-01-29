package com.cibo.evilplot.geometry

/**
  * Extent defines an object's rectangular bounding box.
  * As discussed in <a href="http://ozark.hendrix.edu/~yorgey/pub/monoid-pearl.pdf">
  * "Monoids: Theme and Variations" by Yorgey</a>,
  * rectangular bounding boxes don't play well with rotation.
  * We'll eventually need something fancier like the convex hull.
  *
  * @param width bounding box width
  * @param height bounding box height
  */
case class Extent(width: Double, height: Double) {
  def *(scale: Double): Extent = Extent(scale * width, scale * height)
  def -(w: Double = 0.0, h: Double = 0.0): Extent = Extent(width - w, height - h)
}
