/*
 * Copyright 2017 CiBO Technologies
 */

package com.cibo.evilplot.geometry

case class Extent(width: Double, height: Double) {
  def * (scale: Double): Extent = Extent(width * scale, height * scale)
}
