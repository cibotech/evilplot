package com.cibo.evilplot

import com.cibo.evilplot.colors._
import com.cibo.evilplot.geometry._

object Charts {

  private def createGridLines(maxHeight: Double, width: Double): Renderable =
    DistributeV {
      val lineEveryXUnits     = 40
      val lineThick           = 0.25
      val textHeight          = Text.defaultSize
      val labelFloatAboveLine = 2

      val countOfGridLines = (maxHeight / lineEveryXUnits).toInt

      Seq.tabulate(countOfGridLines){ x =>
        val yHeightLabel = (maxHeight / lineEveryXUnits - x) * lineEveryXUnits

        Pad(bottom = lineEveryXUnits - lineThick){

          val label = Translate(y = -textHeight - labelFloatAboveLine){
            Text(yHeightLabel) filled Grey
          }

          Line(width, lineThick) behind label
        }
      }
    }


  // TODO: this sort of sucks, and textAndPadHeight is a hack that doesnt work right in my barchart case with tick labels?
  private def axis(horizontal: Boolean, maxValue: Double, textAndPadHeight: Double, minValue: Double = 0, doLabelTicks: Boolean = true): Renderable = {

    val rangeSize = maxValue - minValue
    val figureWidth = rangeSize
    val tickThick = figureWidth * 0.0025
    val tickLength = figureWidth * 0.025
    //TODO: Fix dis, extent is being improperly used in some cases, text size should also not be dependent on the with for readability reasons and the scaling is wack
    // requirement failed: Cannot use 0.096, canvas will not render text initially sized < 0.5px even when scaling
    val textSize = (12 / 300.0) * rangeSize
    val tickLabelTextSize = 0.8 * textSize
    val labelEveryKTicks = 5

    val interTickDist = {
      val spacingIfTenTicks = rangeSize / 10D
      // round to nearest multiple of 5 in the scale
      val fiveInTheScale = rangeSize / 20.0 // TODO Is this sane????
      math.min((spacingIfTenTicks / fiveInTheScale).toInt, 1) * fiveInTheScale
    }

    val numTicks = (rangeSize / interTickDist).floor.toInt
    val ticks = Seq.tabulate(numTicks + 1){ i =>
      val tick = Line(tickLength, tickThick) rotated 90 padRight (interTickDist - tickThick)

      tick
    }.distributeH

    val labels = {
      val labelCount = 1 + math.floor(numTicks / labelEveryKTicks).toInt
      val interLabelDist = interTickDist * labelEveryKTicks

      val labelColl = Seq.tabulate(labelCount) { i =>
        val value = i * interLabelDist
        Text(f"${value + minValue}%.1f", tickLabelTextSize) padRight textSize / 4 rotated (if (horizontal) -90 else 0)
      }.reverse

      val combined = DistributeV(
        labelColl,
        interLabelDist - labelColl.head.extent.height
      )

      val leftOverTop = rangeSize - combined.extent.height
      val textCentering = tickLabelTextSize / 3
      combined padTop leftOverTop + textCentering
    }

    val axisTitle = Text("Awesomeness", textSize) rotated (if (horizontal) 0 else -90)
    val linePart = Line(rangeSize, tickThick * 2) behind ticks rotated (if (horizontal) 90 else -90)
    val justAxis = if(doLabelTicks){
      if (horizontal)
        linePart beside (labels padLeft tickLabelTextSize) rotated 90
      else
        (labels padTop interTickDist) beside linePart // todo: why padTop interTickDist ?
    } else {
      if (horizontal) linePart rotated 90 else linePart
    }

    val labeledTickAxis = justAxis padTop textAndPadHeight + tickThick / 2

    if(horizontal)
      Align.center(labeledTickAxis, axisTitle padTop textSize / 2).reduce(Above)
    else
      Align.middle(axisTitle padRight textSize / 2, labeledTickAxis).reduce(Beside)
  }

  private def createBars(heights: Seq[Double], colors: Seq[Color]) = {
    val barWidth = 50
    val barSpacing = 5
    Align.bottomSeq{
      val rects = heights.map { h => Rect(barWidth, h) titled h.toString}
      rects.zip(colors).map { case (rect, color) => rect filled color labeled color.repr }
    }.distributeH(barSpacing)
  }

  def createBarGraph(size: Extent, data: Seq[Double], colors: Seq[Color]): Renderable = {

    val tickThick = 0.5
    val textAndPadHeight = Text.defaultSize + 5 // text size, stroke width

    val barChart = Fit(size){
      val justBars = createBars(data, colors)
      val yAx = axis(false, data.max, 5, doLabelTicks = false) // todo: why is this cheat of 5 necessary still?
      val grid = createGridLines(data.max, justBars.extent.width) padTop textAndPadHeight

      (grid --> yAx.extent.width) behind (yAx beside justBars)
    }

    barChart titled ("A Swanky BarChart", 20) padAll 10
  }

  def createScatterPlot(graphSize: Extent, data: Seq[Point]) = {

    val minX = data.map(_.x).min
    val minY = data.map(_.y).min

    val pointSize = {
      val maxX = data.map(_.x).max
      val maxY = data.map(_.y).max
      math.min(maxX, maxY) / 100.0
    }

    val fitScatter = FlipY(Fit(graphSize){
      val scatter = data.map{ case Point(x, y) => Disc(pointSize, x - math.min(0, minX), y - math.min(0, minY)) }.group
      val xAxis = axis(true, scatter.extent.width, 0)
      val pointAndY = FlipY(axis(false, scatter.extent.height, 0)) beside scatter
      Align.right(pointAndY, FlipY(xAxis)).reverse.reduce(Above)
    })

    fitScatter titled ("A Scatter Plot", 20) padAll 10
  }

  def createLinePlot(graphSize: Extent, data: Seq[Point]) = {

    val fitLine = FlipY(Fit(graphSize){
      val line = Segment(data, 0.5)
      val xAxis = axis(true, line.extent.width, 0)
      val pointAndY = FlipY(axis(false, line.yS.max, 0, line.yS.min)) beside line
      Align.right(pointAndY, FlipY(xAxis)).reverse.reduce(Above)
    })

    fitLine titled ("A Line Plot", 20) padAll 10
  }

  def createMultiLinePlot(graphSize: Extent, datas: Seq[Seq[Point]]) = {

    val fitLine = FlipY(Fit(graphSize){
      val lines = datas.map(data => Segment(data, 0.5))
      val xAxis = axis(true, lines.map(_.xS.max).max, 0, lines.map(_.xS.min).min) // TODO: wut
      val pointAndY = FlipY(axis(false, lines.map(_.yS.max).max, 0, lines.map(_.yS.min).min)) beside lines.group
      Align.right(pointAndY, FlipY(xAxis)).reverse.reduce(Above)
    })

    fitLine titled ("A Multi Line Plot", 20) padAll 10
  }



  def createPieChart(scale: Int, data: Seq[Double]) = {
    val pieWedges = {
      val labelPad = 10 // TODO: should be maxTextWidth?

      // cumulativeRotate is complicated b/c we draw wedges straddling the X axis, but that makes labels easier
      val cumulativeRotate = data.map(_ / 2).sliding(2).map(_.sum).scanLeft(0D)(_+_).toVector
      val wedges = data.zip(cumulativeRotate).map { case (frac, cumRot) =>

        val rotate = 360 * cumRot
        val wedge = UnsafeRotate(rotate)(Wedge(360 * frac, scale))
        val label =
          UnsafeRotate(rotate) {
            val text = {
              val baseText = Text(frac.toString) filled Black
              if (rotate > 90 && rotate < 270) baseText --> (-baseText.extent.width - labelPad) else baseText // TODO: same as left aligned txt?
            }
            val spacer = Disc(scale) filled Clear padRight labelPad

            DistributeH(Align.middle(spacer, UnsafeRotate(-rotate)(text) ))
          }

        wedge behind label
      }

      wedges.zip(Colors.triAnalStream()).map { case (r, color) => r filled color }
    }.group

    val legend = FlowH(
      data.zip(Colors.triAnalStream()).map{ case (d, c) => Rect(scale / 5.0) filled c labeled f"${d*100}%.1f%%" },
      pieWedges.extent
    ) padTop 20

    pieWedges padAll 15 above legend titled("A Smooth Pie Chart", 20) padAll 10
  }
}
