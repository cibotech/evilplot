package com.cibo.evilplot.plot

import com.cibo.evilplot.Text
import com.cibo.evilplot.colors._
import com.cibo.evilplot.geometry._

object Plots {

  def createPieChart(scale: Int, data: Seq[Double]): Pad = {
    val pieWedges = {
      val labelPad = 10 // TODO: should be maxTextWidth?

      // cumulativeRotate is complicated b/c we draw wedges straddling the X axis, but that makes labels easier
      val cumulativeRotate = data.map(_ / 2).sliding(2).map(_.sum).scanLeft(0D)(_ + _).toVector
      val wedges = data.zip(cumulativeRotate).map { case (frac, cumRot) =>

        val rotate = 360 * cumRot
        val wedge = UnsafeRotate(rotate)(Wedge(360 * frac, scale))
        val label =
          UnsafeRotate(rotate) {
            val text = {
              val baseText = Text(frac.toString) filled Black
              // TODO: same as left aligned txt?
              if (rotate > 90 && rotate < 270)
                baseText transX (-baseText.extent.width - labelPad)
              else
                baseText
            }
            val spacer = Disc(scale) filled Clear padRight labelPad

            distributeH(Align.middle(spacer, UnsafeRotate(-rotate)(text)))
          }

        wedge behind label
      }

      wedges.zip(Colors.triAnalogStream()).map { case (r, color) => r filled color }
    }.group

    val legend = flowH(
      data.zip(Colors.triAnalogStream()).map { case (d, c) => Rect(scale / 5.0) filled c labeled f"${d * 100}%.1f%%" },
      pieWedges.extent
    ) padTop 20

    pieWedges padAll 15 above legend titled("A Smooth Pie Chart", 20) padAll 10
  }
}

