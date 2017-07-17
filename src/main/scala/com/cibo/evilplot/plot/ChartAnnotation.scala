/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.plot

import com.cibo.evilplot.Text
import com.cibo.evilplot.geometry.{Above, Align, Drawable, WrapDrawable}

// TODO: More options for placement / not necessarily centered
class ChartAnnotation(text: Seq[String], val position: (Double, Double), fontSize: Double = 12) extends WrapDrawable {
  private val annotation = Align.centerSeq(text.map(Text(_, fontSize))).reduce(Above)
  override def drawable: Drawable = annotation
}
