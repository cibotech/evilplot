package com.cibo.evilplot.plot

import com.cibo.evilplot.colors.Colors.ColorSeq
import com.cibo.evilplot.colors.{Clear, HTMLNamedColors}
import com.cibo.evilplot.geometry.{Align, Disc, Drawable, Extent, Rect, UnsafeRotate, Wedge, distributeH, flowH, _}
import com.cibo.evilplot.plotdefs.PlotOptions
import org.scalajs.dom.CanvasRenderingContext2D

case class PieChart(
  extent: Extent,
  labels: Option[Seq[String]] = None,
  data: Seq[Double],
  options: PlotOptions,
  scale: Double = 100.0
) {

  // generate labels as percent values, if labels not passed in
  private val labs = if (labels.isDefined) {
    require(labels.get.length == data.length, "Labels and data must have the same length.")
    labels.get
  }
  else {
    val tot = data.sum
    data.map { x => f"${x/tot * 100}%.1f%%" }
  }

  private val _drawable = {
    val pieWedges = {
      val labelPad = 10 // TODO: should be maxTextWidth?

      // cumulativeRotate is complicated b/c we draw wedges straddling the X axis, but that makes labels easier
      val cumulativeRotate = data.map(_ / 2).sliding(2).map(_.sum).scanLeft(0D)(_ + _).toVector
      val wedges: Seq[Group] = data.zip(cumulativeRotate).map { case (frac, cumRot) =>

        val rotate = 360 * cumRot
        val wedge = UnsafeRotate(rotate)(Wedge(360 * frac, scale))
        val label =
          UnsafeRotate(rotate) {
            val text = {
              val baseText = Text(frac.toString) filled HTMLNamedColors.black
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

      wedges.zip(ColorSeq.getGradientSeq(wedges.length)).map { case (r, color) => r filled color }
    }.group

    val legend = flowH(
      data.zip(ColorSeq.getGradientSeq(data.length)).map {
        case (d, c) => Rect(scale / 5.0) filled c labeled f"${d * 100}%.1f%%" },
      pieWedges.extent
    ) padTop 20

    pieWedges padAll 15 above legend padAll 10
  }

  def drawable: Drawable = _drawable titled(options.title.getOrElse(""), 20)

}
