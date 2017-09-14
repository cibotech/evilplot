/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.plot
import com.cibo.evilplot.Text
import com.cibo.evilplot.geometry._

// TODO: More options for placement / not necessarily centered
case class ChartAnnotation(text: Seq[String], position: (Double, Double), fontSize: Double = 12) extends WrapDrawable {
  override def drawable: Drawable = if (text.nonEmpty)
    Align.centerSeq(text.map(Text(_, fontSize))).reduce(Above) else EmptyDrawable()
}

