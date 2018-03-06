package com.cibo.evilplot.plot

import com.cibo.evilplot.colors.{Color, HTMLNamedColors}
import com.cibo.evilplot.geometry.{Drawable, Extent, Rect, Text, Wedge}
import com.cibo.evilplot.numeric.Bounds
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.renderers.PlotRenderer

object PieChart {

  case class PieChartRenderer(
    data: Seq[(Drawable, Double)],
    colors: Seq[Color]
  ) extends PlotRenderer {

    override def legendContext: LegendContext = {
      val labelColors = data.map(_._1).zip(colors)
      LegendContext(
        elements = labelColors.map(lc => Rect(Text.defaultSize, Text.defaultSize).filled(lc._2)),
        labels = labelColors.map(_._1),
        defaultStyle = LegendStyle.Categorical
      )
    }

    def render(plot: Plot, plotExtent: Extent)(implicit theme: Theme): Drawable = {

      val radius = math.min(plotExtent.width, plotExtent.height) / 2

      val total: Double = data.map(_._2).sum
      val totalRotations = data.tail.scanLeft(data.head._2 * 360 / total) { case (acc, (_, value)) =>
        acc + value * 360.0 / total
      } :+ 360.0

      val wedges = data.zip(totalRotations).zip(colors).map { case (((_, value), totalRotation), color) =>
        Wedge(totalRotation, radius).filled(color)
      }

      val labels = data.zip(totalRotations).map { case ((label, value), totalRotation) =>
        val radians = math.toRadians(totalRotation - value * 180 / total)
        val xoffset = math.cos(radians) * radius / 2
        val yoffset = math.sin(radians) * radius / 2
        label.translate(x = radius + xoffset, y = radius + yoffset)
      }

      wedges.reverse.group behind labels.group
    }
  }

  def apply(
    data: Seq[(String, Double)],
    colors: Seq[Color] = Color.stream,
    textColor: Color = HTMLNamedColors.black,
    textSize: Double = Text.defaultSize
  ): Plot = {
    val withLabels = data.map(v => Text(v._1, textSize).filled(textColor) -> v._2)
    Plot(
      xbounds = Bounds(0, 1),
      ybounds = Bounds(0, 1),
      renderer = PieChartRenderer(withLabels, colors)
    )
  }

}
